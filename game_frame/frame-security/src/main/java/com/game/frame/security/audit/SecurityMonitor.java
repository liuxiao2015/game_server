package com.game.frame.security.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 安全监控器
 * 实现实时安全事件监控、异常告警、统计分析
 * @author lx
 * @date 2025/06/08
 */
@Component
public class SecurityMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityMonitor.class);
    
    private static final String SECURITY_STATS_PREFIX = "security:stats:";
    private static final String ALERT_THRESHOLD_PREFIX = "security:threshold:";
    private static final String INCIDENT_PREFIX = "security:incident:";
    
    // 告警阈值配置
    private static final int LOGIN_FAILURE_THRESHOLD = 5; // 5次登录失败
    private static final int ATTACK_THRESHOLD = 3; // 3次攻击尝试
    private static final int ABNORMAL_ACCESS_THRESHOLD = 10; // 10次异常访问
    private static final int TIME_WINDOW_MINUTES = 15; // 15分钟时间窗口
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private SecurityEventPublisher eventPublisher;
    
    @Autowired
    private AuditService auditService;
    
    /**
     * 监听安全事件
     */
    @EventListener
    @Async
    public void handleSecurityEvent(SecurityEvent event) {
        try {
            // 记录事件统计
            recordEventStatistics(event);
            
            // 检查告警条件
            checkAlertConditions(event);
            
            // 记录到审计日志
            auditService.recordSecurityEvent(event);
            
            // 处理高严重性事件
            if (event.getSeverity().requiresImmediateNotification()) {
                handleHighSeverityEvent(event);
            }
            
            logger.debug("Security event processed: {}", event.getEventType());
            
        } catch (Exception e) {
            logger.error("Failed to handle security event: {}", event.getEventType(), e);
        }
    }
    
    /**
     * 记录事件统计信息
     */
    private void recordEventStatistics(SecurityEvent event) {
        try {
            long currentTime = System.currentTimeMillis();
            long timeWindow = currentTime / (TIME_WINDOW_MINUTES * 60 * 1000);
            
            // 总体统计
            String globalKey = SECURITY_STATS_PREFIX + "global:" + timeWindow;
            redisTemplate.opsForHash().increment(globalKey, "total", 1);
            redisTemplate.opsForHash().increment(globalKey, event.getEventType().name(), 1);
            redisTemplate.opsForHash().increment(globalKey, event.getSeverity().name(), 1);
            redisTemplate.expire(globalKey, 24, TimeUnit.HOURS);
            
            // IP统计
            if (event.getSourceIp() != null) {
                String ipKey = SECURITY_STATS_PREFIX + "ip:" + event.getSourceIp() + ":" + timeWindow;
                redisTemplate.opsForHash().increment(ipKey, "total", 1);
                redisTemplate.opsForHash().increment(ipKey, event.getEventType().name(), 1);
                redisTemplate.expire(ipKey, 24, TimeUnit.HOURS);
            }
            
            // 用户统计
            if (event.getUserId() != null) {
                String userKey = SECURITY_STATS_PREFIX + "user:" + event.getUserId() + ":" + timeWindow;
                redisTemplate.opsForHash().increment(userKey, "total", 1);
                redisTemplate.opsForHash().increment(userKey, event.getEventType().name(), 1);
                redisTemplate.expire(userKey, 24, TimeUnit.HOURS);
            }
            
        } catch (Exception e) {
            logger.error("Failed to record event statistics", e);
        }
    }
    
    /**
     * 检查告警条件
     */
    private void checkAlertConditions(SecurityEvent event) {
        try {
            long currentTime = System.currentTimeMillis();
            long timeWindow = currentTime / (TIME_WINDOW_MINUTES * 60 * 1000);
            
            switch (event.getEventType()) {
                case LOGIN_FAILURE:
                    checkLoginFailureAlert(event, timeWindow);
                    break;
                case SECURITY_ATTACK:
                case SQL_INJECTION_ATTEMPT:
                case XSS_ATTEMPT:
                    checkAttackAlert(event, timeWindow);
                    break;
                case ABNORMAL_ACCESS:
                    checkAbnormalAccessAlert(event, timeWindow);
                    break;
                case DATA_LEAKAGE_RISK:
                    // 数据泄露风险立即告警
                    triggerImmediateAlert(event, "Data leakage risk detected");
                    break;
                default:
                    // 其他事件的通用检查
                    checkGeneralAlert(event, timeWindow);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Failed to check alert conditions", e);
        }
    }
    
    /**
     * 检查登录失败告警
     */
    private void checkLoginFailureAlert(SecurityEvent event, long timeWindow) {
        if (event.getSourceIp() == null) return;
        
        String ipKey = SECURITY_STATS_PREFIX + "ip:" + event.getSourceIp() + ":" + timeWindow;
        Object count = redisTemplate.opsForHash().get(ipKey, SecurityEventType.LOGIN_FAILURE.name());
        
        if (count != null && Integer.parseInt(count.toString()) >= LOGIN_FAILURE_THRESHOLD) {
            triggerAlert(event, "Multiple login failures from IP: " + event.getSourceIp(), 
                        SecuritySeverity.HIGH);
        }
    }
    
    /**
     * 检查攻击告警
     */
    private void checkAttackAlert(SecurityEvent event, long timeWindow) {
        if (event.getSourceIp() == null) return;
        
        String ipKey = SECURITY_STATS_PREFIX + "ip:" + event.getSourceIp() + ":" + timeWindow;
        long attackCount = 0;
        
        // 统计各种攻击类型
        for (SecurityEventType attackType : Arrays.asList(
                SecurityEventType.SECURITY_ATTACK,
                SecurityEventType.SQL_INJECTION_ATTEMPT,
                SecurityEventType.XSS_ATTEMPT,
                SecurityEventType.CSRF_ATTEMPT)) {
            Object count = redisTemplate.opsForHash().get(ipKey, attackType.name());
            if (count != null) {
                attackCount += Integer.parseInt(count.toString());
            }
        }
        
        if (attackCount >= ATTACK_THRESHOLD) {
            triggerAlert(event, "Multiple attack attempts from IP: " + event.getSourceIp(), 
                        SecuritySeverity.CRITICAL);
        }
    }
    
    /**
     * 检查异常访问告警
     */
    private void checkAbnormalAccessAlert(SecurityEvent event, long timeWindow) {
        if (event.getSourceIp() == null) return;
        
        String ipKey = SECURITY_STATS_PREFIX + "ip:" + event.getSourceIp() + ":" + timeWindow;
        Object count = redisTemplate.opsForHash().get(ipKey, SecurityEventType.ABNORMAL_ACCESS.name());
        
        if (count != null && Integer.parseInt(count.toString()) >= ABNORMAL_ACCESS_THRESHOLD) {
            triggerAlert(event, "Multiple abnormal access from IP: " + event.getSourceIp(), 
                        SecuritySeverity.HIGH);
        }
    }
    
    /**
     * 通用告警检查
     */
    private void checkGeneralAlert(SecurityEvent event, long timeWindow) {
        if (event.getSeverity().requiresImmediateNotification()) {
            triggerAlert(event, "High severity security event: " + event.getEventType(), 
                        event.getSeverity());
        }
    }
    
    /**
     * 触发告警
     */
    private void triggerAlert(SecurityEvent event, String alertMessage, SecuritySeverity severity) {
        try {
            SecurityAlert alert = new SecurityAlert(
                UUID.randomUUID().toString(),
                event.getEventType(),
                severity,
                alertMessage,
                event.getSourceIp(),
                event.getUserId(),
                LocalDateTime.now(),
                createAlertDetails(event)
            );
            
            // 记录告警
            recordAlert(alert);
            
            // 发送通知
            sendAlert(alert);
            
            logger.warn("Security alert triggered: {}", alertMessage);
            
        } catch (Exception e) {
            logger.error("Failed to trigger alert", e);
        }
    }
    
    /**
     * 触发立即告警
     */
    private void triggerImmediateAlert(SecurityEvent event, String message) {
        triggerAlert(event, message, SecuritySeverity.EMERGENCY);
    }
    
    /**
     * 处理高严重性事件
     */
    private void handleHighSeverityEvent(SecurityEvent event) {
        try {
            // 创建安全事件单
            createSecurityIncident(event);
            
            // 如果是紧急事件，立即执行响应措施
            if (event.getSeverity() == SecuritySeverity.EMERGENCY) {
                executeEmergencyResponse(event);
            }
            
        } catch (Exception e) {
            logger.error("Failed to handle high severity event", e);
        }
    }
    
    /**
     * 创建安全事件单
     */
    private void createSecurityIncident(SecurityEvent event) {
        try {
            String incidentId = UUID.randomUUID().toString();
            String incidentKey = INCIDENT_PREFIX + incidentId;
            
            Map<String, String> incidentData = new HashMap<>();
            incidentData.put("id", incidentId);
            incidentData.put("eventType", event.getEventType().name());
            incidentData.put("severity", event.getSeverity().name());
            incidentData.put("sourceIp", event.getSourceIp());
            incidentData.put("userId", event.getUserId());
            incidentData.put("message", event.getMessage());
            incidentData.put("timestamp", event.getTimestamp().toString());
            incidentData.put("status", "OPEN");
            
            redisTemplate.opsForHash().putAll(incidentKey, incidentData);
            redisTemplate.expire(incidentKey, 30, TimeUnit.DAYS);
            
            logger.info("Security incident created: {}", incidentId);
            
        } catch (Exception e) {
            logger.error("Failed to create security incident", e);
        }
    }
    
    /**
     * 执行紧急响应
     */
    private void executeEmergencyResponse(SecurityEvent event) {
        try {
            // 根据事件类型执行不同的响应措施
            switch (event.getEventType()) {
                case DDOS_ATTACK:
                    // 触发DDoS防护
                    activateDDoSProtection(event.getSourceIp());
                    break;
                case DATA_LEAKAGE_RISK:
                    // 暂停相关账户
                    suspendAccount(event.getUserId());
                    break;
                case SYSTEM_INTRUSION:
                    // 隔离受影响的系统
                    isolateSystem(event.getSourceIp());
                    break;
                default:
                    logger.warn("No specific emergency response for event type: {}", event.getEventType());
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Failed to execute emergency response", e);
        }
    }
    
    /**
     * 激活DDoS防护
     */
    private void activateDDoSProtection(String sourceIp) {
        // TODO: 实现DDoS防护激活逻辑
        logger.warn("DDoS protection activated for IP: {}", sourceIp);
    }
    
    /**
     * 暂停账户
     */
    private void suspendAccount(String userId) {
        // TODO: 实现账户暂停逻辑
        logger.warn("Account suspended: {}", userId);
    }
    
    /**
     * 隔离系统
     */
    private void isolateSystem(String sourceIp) {
        // TODO: 实现系统隔离逻辑
        logger.warn("System isolation activated for IP: {}", sourceIp);
    }
    
    /**
     * 记录告警
     */
    private void recordAlert(SecurityAlert alert) {
        // TODO: 将告警记录到数据库或其他持久化存储
        logger.info("Alert recorded: {}", alert.getAlertId());
    }
    
    /**
     * 发送告警通知
     */
    private void sendAlert(SecurityAlert alert) {
        // TODO: 实现告警通知发送（邮件、短信、钉钉等）
        logger.warn("Alert sent: {} - {}", alert.getSeverity(), alert.getMessage());
    }
    
    /**
     * 创建告警详情
     */
    private Map<String, Object> createAlertDetails(SecurityEvent event) {
        Map<String, Object> details = new HashMap<>();
        details.put("eventId", event.getEventId());
        details.put("eventType", event.getEventType().name());
        details.put("originalMessage", event.getMessage());
        details.put("userAgent", event.getUserAgent());
        details.putAll(event.getDetails());
        return details;
    }
    
    /**
     * 定期安全统计分析
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void performSecurityAnalysis() {
        try {
            logger.info("Starting security analysis...");
            
            // 分析登录失败趋势
            analyzeLoginFailureTrends();
            
            // 分析攻击模式
            analyzeAttackPatterns();
            
            // 分析异常IP
            analyzeAbnormalIPs();
            
            logger.info("Security analysis completed");
            
        } catch (Exception e) {
            logger.error("Failed to perform security analysis", e);
        }
    }
    
    private void analyzeLoginFailureTrends() {
        // TODO: 实现登录失败趋势分析
        logger.debug("Analyzing login failure trends...");
    }
    
    private void analyzeAttackPatterns() {
        // TODO: 实现攻击模式分析
        logger.debug("Analyzing attack patterns...");
    }
    
    private void analyzeAbnormalIPs() {
        // TODO: 实现异常IP分析
        logger.debug("Analyzing abnormal IPs...");
    }
    
    /**
     * 安全告警内部类
     */
    public static class SecurityAlert {
        private String alertId;
        private SecurityEventType eventType;
        private SecuritySeverity severity;
        private String message;
        private String sourceIp;
        private String userId;
        private LocalDateTime timestamp;
        private Map<String, Object> details;
        
        public SecurityAlert(String alertId, SecurityEventType eventType, SecuritySeverity severity,
                           String message, String sourceIp, String userId, LocalDateTime timestamp,
                           Map<String, Object> details) {
            this.alertId = alertId;
            this.eventType = eventType;
            this.severity = severity;
            this.message = message;
            this.sourceIp = sourceIp;
            this.userId = userId;
            this.timestamp = timestamp;
            this.details = details;
        }
        
        // Getters
        public String getAlertId() { return alertId; }
        public SecurityEventType getEventType() { return eventType; }
        public SecuritySeverity getSeverity() { return severity; }
        public String getMessage() { return message; }
        public String getSourceIp() { return sourceIp; }
        public String getUserId() { return userId; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public Map<String, Object> getDetails() { return details; }
    }
}