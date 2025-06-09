package com.game.frame.security.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 安全事件发布器
 * 发布各种安全事件：登录成功/失败、权限拒绝、异常访问、数据泄露风险
 * @author lx
 * @date 2025/06/08
 */
@Component
public class SecurityEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityEventPublisher.class);
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private AuditService auditService;
    
    /**
     * 发布登录成功事件
     */
    public void publishLoginSuccessEvent(String userId, String username, String ip, String userAgent) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.LOGIN_SUCCESS)
                .userId(userId)
                .username(username)
                .sourceIp(ip)
                .userAgent(userAgent)
                .severity(SecuritySeverity.INFO)
                .message("User login successful")
                .timestamp(LocalDateTime.now())
                .build();
            
            eventPublisher.publishEvent(event);
            logger.debug("Published login success event for user: {}", username);
            
        } catch (Exception e) {
            logger.error("Failed to publish login success event for user: {}", username, e);
        }
    }
    
    /**
     * 发布登录失败事件
     */
    public void publishLoginFailureEvent(String username, String ip, String reason, String userAgent) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.LOGIN_FAILURE)
                .username(username)
                .sourceIp(ip)
                .userAgent(userAgent)
                .severity(SecuritySeverity.WARNING)
                .message("User login failed: " + reason)
                .timestamp(LocalDateTime.now())
                .detail("reason", reason)
                .build();
            
            eventPublisher.publishEvent(event);
            logger.warn("Published login failure event for user: {} reason: {}", username, reason);
            
        } catch (Exception e) {
            logger.error("Failed to publish login failure event for user: {}", username, e);
        }
    }
    
    /**
     * 发布权限拒绝事件
     */
    public void publishPermissionDeniedEvent(String userId, String username, String resource, String action, String ip) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.PERMISSION_DENIED)
                .userId(userId)
                .username(username)
                .sourceIp(ip)
                .severity(SecuritySeverity.WARNING)
                .message("Permission denied for resource: " + resource + " action: " + action)
                .timestamp(LocalDateTime.now())
                .detail("resource", resource)
                .detail("action", action)
                .build();
            
            eventPublisher.publishEvent(event);
            logger.warn("Published permission denied event for user: {} resource: {} action: {}", username, resource, action);
            
        } catch (Exception e) {
            logger.error("Failed to publish permission denied event for user: {}", username, e);
        }
    }
    
    /**
     * 发布异常访问事件
     */
    public void publishAbnormalAccessEvent(String userId, String username, String ip, String reason, Map<String, Object> details) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.ABNORMAL_ACCESS)
                .userId(userId)
                .username(username)
                .sourceIp(ip)
                .severity(SecuritySeverity.HIGH)
                .message("Abnormal access detected: " + reason)
                .timestamp(LocalDateTime.now())
                .detail("reason", reason)
                .details(details)
                .build();
            
            eventPublisher.publishEvent(event);
            logger.warn("Published abnormal access event for user: {} reason: {}", username, reason);
            
        } catch (Exception e) {
            logger.error("Failed to publish abnormal access event for user: {}", username, e);
        }
    }
    
    /**
     * 发布数据泄露风险事件
     */
    public void publishDataLeakageRiskEvent(String userId, String username, String dataType, String action, String ip) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.DATA_LEAKAGE_RISK)
                .userId(userId)
                .username(username)
                .sourceIp(ip)
                .severity(SecuritySeverity.CRITICAL)
                .message("Data leakage risk detected: " + dataType + " " + action)
                .timestamp(LocalDateTime.now())
                .detail("dataType", dataType)
                .detail("action", action)
                .build();
            
            eventPublisher.publishEvent(event);
            logger.error("Published data leakage risk event for user: {} dataType: {}", username, dataType);
            
        } catch (Exception e) {
            logger.error("Failed to publish data leakage risk event for user: {}", username, e);
        }
    }
    
    /**
     * 发布安全攻击事件
     */
    public void publishSecurityAttackEvent(String ip, String attackType, String details, String userAgent) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.SECURITY_ATTACK)
                .sourceIp(ip)
                .userAgent(userAgent)
                .severity(SecuritySeverity.CRITICAL)
                .message("Security attack detected: " + attackType)
                .timestamp(LocalDateTime.now())
                .detail("attackType", attackType)
                .detail("details", details)
                .build();
            
            eventPublisher.publishEvent(event);
            logger.error("Published security attack event from IP: {} type: {}", ip, attackType);
            
        } catch (Exception e) {
            logger.error("Failed to publish security attack event from IP: {}", ip, e);
        }
    }
    
    /**
     * 发布账户锁定事件
     */
    public void publishAccountLockedEvent(String username, String ip, String reason) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.ACCOUNT_LOCKED)
                .username(username)
                .sourceIp(ip)
                .severity(SecuritySeverity.HIGH)
                .message("Account locked: " + reason)
                .timestamp(LocalDateTime.now())
                .detail("reason", reason)
                .build();
            
            eventPublisher.publishEvent(event);
            logger.warn("Published account locked event for user: {} reason: {}", username, reason);
            
        } catch (Exception e) {
            logger.error("Failed to publish account locked event for user: {}", username, e);
        }
    }
    
    /**
     * 发布密码修改事件
     */
    public void publishPasswordChangeEvent(String userId, String username, String ip, boolean success) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.PASSWORD_CHANGE)
                .userId(userId)
                .username(username)
                .sourceIp(ip)
                .severity(success ? SecuritySeverity.INFO : SecuritySeverity.WARNING)
                .message("Password change " + (success ? "successful" : "failed"))
                .timestamp(LocalDateTime.now())
                .detail("success", String.valueOf(success))
                .build();
            
            eventPublisher.publishEvent(event);
            logger.debug("Published password change event for user: {} success: {}", username, success);
            
        } catch (Exception e) {
            logger.error("Failed to publish password change event for user: {}", username, e);
        }
    }
    
    /**
     * 发布Token异常事件
     */
    public void publishTokenAnomalyEvent(String userId, String username, String tokenType, String anomaly, String ip) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.TOKEN_ANOMALY)
                .userId(userId)
                .username(username)
                .sourceIp(ip)
                .severity(SecuritySeverity.HIGH)
                .message("Token anomaly detected: " + anomaly)
                .timestamp(LocalDateTime.now())
                .detail("tokenType", tokenType)
                .detail("anomaly", anomaly)
                .build();
            
            eventPublisher.publishEvent(event);
            logger.warn("Published token anomaly event for user: {} anomaly: {}", username, anomaly);
            
        } catch (Exception e) {
            logger.error("Failed to publish token anomaly event for user: {}", username, e);
        }
    }
    
    /**
     * 发布配置变更事件
     */
    public void publishConfigChangeEvent(String userId, String username, String configType, String operation, String ip) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(SecurityEventType.CONFIG_CHANGE)
                .userId(userId)
                .username(username)
                .sourceIp(ip)
                .severity(SecuritySeverity.MEDIUM)
                .message("Configuration changed: " + configType + " " + operation)
                .timestamp(LocalDateTime.now())
                .detail("configType", configType)
                .detail("operation", operation)
                .build();
            
            eventPublisher.publishEvent(event);
            logger.info("Published config change event for user: {} type: {} operation: {}", username, configType, operation);
            
        } catch (Exception e) {
            logger.error("Failed to publish config change event for user: {}", username, e);
        }
    }
    
    /**
     * 发布自定义安全事件
     */
    public void publishCustomSecurityEvent(SecurityEventType eventType, String userId, String username, 
                                         String ip, SecuritySeverity severity, String message, 
                                         Map<String, Object> details) {
        try {
            SecurityEvent event = SecurityEvent.builder()
                .eventType(eventType)
                .userId(userId)
                .username(username)
                .sourceIp(ip)
                .severity(severity)
                .message(message)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
            
            eventPublisher.publishEvent(event);
            logger.debug("Published custom security event: {} for user: {}", eventType, username);
            
        } catch (Exception e) {
            logger.error("Failed to publish custom security event for user: {}", username, e);
        }
    }
}