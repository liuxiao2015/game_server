package com.game.frame.security.audit;

import com.game.frame.security.auth.AuthUser;
import com.game.frame.security.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 审计服务
 * @author lx
 * @date 2025/06/08
 */
@Service
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    private static final String AUDIT_LOG_PREFIX = "security:audit:";
    
    // In-memory queue for batch processing
    private final BlockingQueue<AuditLog> auditQueue = new LinkedBlockingQueue<>();

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 异步记录审计日志
     */
    @Async
    public void logAsync(AuditLog auditLog) {
        try {
            if (securityProperties.getAudit().isEnabled()) {
                if (securityProperties.getAudit().isAsync()) {
                    // Add to queue for batch processing
                    auditQueue.offer(auditLog);
                } else {
                    // Log immediately
                    logAuditEntry(auditLog);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to log audit entry", e);
        }
    }

    /**
     * 同步记录审计日志
     */
    public void log(AuditLog auditLog) {
        try {
            if (securityProperties.getAudit().isEnabled()) {
                logAuditEntry(auditLog);
            }
        } catch (Exception e) {
            logger.error("Failed to log audit entry", e);
        }
    }

    /**
     * 记录用户操作
     */
    public void logUserAction(String action, String resource, HttpServletRequest request) {
        AuthUser user = getCurrentUser();
        AuditLog auditLog = new AuditLog();
        
        if (user != null) {
            auditLog.setUserId(user.getUserId().toString());
            auditLog.setUsername(user.getUsername());
        }
        
        auditLog.setAction(action);
        auditLog.setResource(resource);
        
        if (request != null) {
            auditLog.setMethod(request.getMethod());
            auditLog.setPath(request.getRequestURI());
            auditLog.setIp(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        logAsync(auditLog);
    }

    /**
     * 记录用户操作（带详细信息）
     */
    public void logUserAction(String action, String resource, Map<String, Object> details, HttpServletRequest request) {
        AuthUser user = getCurrentUser();
        AuditLog auditLog = new AuditLog();
        
        if (user != null) {
            auditLog.setUserId(user.getUserId().toString());
            auditLog.setUsername(user.getUsername());
        }
        
        auditLog.setAction(action);
        auditLog.setResource(resource);
        auditLog.setDetails(details);
        
        if (request != null) {
            auditLog.setMethod(request.getMethod());
            auditLog.setPath(request.getRequestURI());
            auditLog.setIp(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        logAsync(auditLog);
    }

    /**
     * 记录安全事件
     */
    public void logSecurityEvent(String action, String message, String ip) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setResource("SECURITY_EVENT");
        auditLog.setErrorMessage(message);
        auditLog.setIp(ip);
        auditLog.setStatus("SECURITY_EVENT");
        
        logAsync(auditLog);
    }

    /**
     * 记录登录事件
     */
    public void logLogin(String userId, String username, String ip, boolean success, String errorMessage) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setAction("LOGIN");
        auditLog.setResource("USER_SESSION");
        auditLog.setIp(ip);
        auditLog.setStatus(success ? "SUCCESS" : "FAILED");
        auditLog.setErrorMessage(errorMessage);
        
        logAsync(auditLog);
    }

    /**
     * 记录权限拒绝事件
     */
    public void logAccessDenied(String resource, String requiredPermission) {
        AuthUser user = getCurrentUser();
        AuditLog auditLog = new AuditLog();
        
        if (user != null) {
            auditLog.setUserId(user.getUserId().toString());
            auditLog.setUsername(user.getUsername());
        }
        
        auditLog.setAction("ACCESS_DENIED");
        auditLog.setResource(resource);
        auditLog.setStatus("DENIED");
        auditLog.setErrorMessage("Required permission: " + requiredPermission);
        
        logAsync(auditLog);
    }

    /**
     * 批量写入审计日志
     */
    public void flushAuditLogs() {
        if (!securityProperties.getAudit().isAsync()) {
            return;
        }

        List<AuditLog> batch = new ArrayList<>();
        int batchSize = securityProperties.getAudit().getBatchSize();
        
        try {
            // Drain up to batchSize logs from queue
            for (int i = 0; i < batchSize; i++) {
                AuditLog log = auditQueue.poll(100, TimeUnit.MILLISECONDS);
                if (log == null) break;
                batch.add(log);
            }
            
            if (!batch.isEmpty()) {
                // Write batch to storage
                for (AuditLog log : batch) {
                    logAuditEntry(log);
                }
                logger.debug("Flushed {} audit logs", batch.size());
            }
            
        } catch (Exception e) {
            logger.error("Failed to flush audit logs", e);
            // Put logs back in queue
            auditQueue.addAll(batch);
        }
    }

    /**
     * 实际记录审计日志
     */
    private void logAuditEntry(AuditLog auditLog) {
        try {
            // Generate ID if not set
            if (auditLog.getId() == null) {
                auditLog.setId(UUID.randomUUID().toString());
            }
            
            // Log to application logger (will be picked up by log aggregation)
            auditLogger.info("AUDIT: {}", auditLog);
            
            // Store in Redis for real-time queries (optional)
            String key = AUDIT_LOG_PREFIX + System.currentTimeMillis() + ":" + auditLog.getId();
            redisTemplate.opsForValue().set(key, auditLog, 7, TimeUnit.DAYS);
            
        } catch (Exception e) {
            logger.error("Failed to write audit log entry", e);
        }
    }

    /**
     * 获取当前用户
     */
    private AuthUser getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
                return (AuthUser) authentication.getPrincipal();
            }
        } catch (Exception e) {
            logger.debug("Failed to get current user for audit", e);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 记录安全事件到审计日志
     */
    @Async
    public void recordSecurityEvent(SecurityEvent event) {
        try {
            AuditLog auditLog = new AuditLog(
                event.getUserId(),
                event.getEventType().name(),
                "SECURITY_EVENT"
            );
            
            // 设置其他属性
            auditLog.setId(event.getEventId());
            auditLog.setUsername(event.getUsername());
            auditLog.setIp(event.getSourceIp());
            auditLog.setUserAgent(event.getUserAgent());
            auditLog.setTimestamp(event.getTimestamp());
            auditLog.setDetails(event.getDetails());
            auditLog.setStatus("SUCCESS");
            auditLog.setErrorMessage(event.getMessage());
            
            // 异步记录
            logAsync(auditLog);
            
            logger.debug("Security event recorded to audit log: {}", event.getEventType());
            
        } catch (Exception e) {
            logger.error("Failed to record security event to audit log", e);
        }
    }

    /**
     * 查询审计日志
     */
    public List<AuditLog> queryAuditLogs(String userId, String action, LocalDateTime startTime, LocalDateTime endTime) {
        // This is a simplified implementation
        // In production, you'd use a proper database or search engine
        List<AuditLog> results = new ArrayList<>();
        
        try {
            // Query from Redis (limited time range)
            // In production, use proper audit log storage
            logger.info("Querying audit logs for user: {}, action: {}", userId, action);
            
        } catch (Exception e) {
            logger.error("Failed to query audit logs", e);
        }
        
        return results;
    }
}