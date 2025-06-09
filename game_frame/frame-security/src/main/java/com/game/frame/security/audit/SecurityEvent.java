package com.game.frame.security.audit;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 安全事件实体
 * @author lx
 * @date 2025/06/08
 */
public class SecurityEvent {
    
    private String eventId;
    private SecurityEventType eventType;
    private String userId;
    private String username;
    private String sourceIp;
    private String userAgent;
    private SecuritySeverity severity;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> details;
    
    public SecurityEvent() {
        this.details = new HashMap<>();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
    
    // Builder pattern
    public static SecurityEventBuilder builder() {
        return new SecurityEventBuilder();
    }
    
    public static class SecurityEventBuilder {
        private SecurityEvent event = new SecurityEvent();
        
        public SecurityEventBuilder eventType(SecurityEventType eventType) {
            event.eventType = eventType;
            return this;
        }
        
        public SecurityEventBuilder userId(String userId) {
            event.userId = userId;
            return this;
        }
        
        public SecurityEventBuilder username(String username) {
            event.username = username;
            return this;
        }
        
        public SecurityEventBuilder sourceIp(String sourceIp) {
            event.sourceIp = sourceIp;
            return this;
        }
        
        public SecurityEventBuilder userAgent(String userAgent) {
            event.userAgent = userAgent;
            return this;
        }
        
        public SecurityEventBuilder severity(SecuritySeverity severity) {
            event.severity = severity;
            return this;
        }
        
        public SecurityEventBuilder message(String message) {
            event.message = message;
            return this;
        }
        
        public SecurityEventBuilder timestamp(LocalDateTime timestamp) {
            event.timestamp = timestamp;
            return this;
        }
        
        public SecurityEventBuilder detail(String key, Object value) {
            event.details.put(key, value);
            return this;
        }
        
        public SecurityEventBuilder details(Map<String, Object> details) {
            if (details != null) {
                event.details.putAll(details);
            }
            return this;
        }
        
        public SecurityEvent build() {
            return event;
        }
    }
    
    // Getters and setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    
    public SecurityEventType getEventType() { return eventType; }
    public void setEventType(SecurityEventType eventType) { this.eventType = eventType; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String sourceIp) { this.sourceIp = sourceIp; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public SecuritySeverity getSeverity() { return severity; }
    public void setSeverity(SecuritySeverity severity) { this.severity = severity; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
    
    @Override
    public String toString() {
        return String.format("SecurityEvent{eventId='%s', eventType=%s, username='%s', sourceIp='%s', severity=%s, message='%s', timestamp=%s}",
                eventId, eventType, username, sourceIp, severity, message, timestamp);
    }
}