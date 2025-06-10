package com.game.frame.security.audit;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 安全事件实体类
 * 
 * 功能说明：
 * - 记录和管理系统中发生的各类安全事件
 * - 提供结构化的安全事件数据模型，支持事件分析和响应
 * - 集成建造者模式，简化安全事件对象的创建和配置
 * - 支持安全事件的分级管理和详细信息存储
 * 
 * 设计思路：
 * - 采用建造者模式，提供链式调用的流畅API
 * - 自动生成唯一事件ID，便于事件追踪和关联
 * - 支持严重级别分类，便于安全事件的优先级处理
 * - 提供扩展字段存储，适应不同类型安全事件的特殊需求
 * 
 * 安全事件类型：
 * - 认证事件：登录失败、暴力破解、异地登录等
 * - 授权事件：权限越权、非法访问、权限变更等
 * - 数据事件：敏感数据访问、数据泄露、数据篡改等
 * - 系统事件：系统入侵、恶意代码、系统异常等
 * 
 * 应用场景：
 * - 安全监控系统的事件收集和分析
 * - 安全运营中心（SOC）的事件管理
 * - 合规审计和安全事件追溯
 * - 安全威胁的实时检测和响应
 * 
 * 建造者模式优势：
 * - 支持链式调用，代码可读性强
 * - 参数可选配置，适应不同事件场景
 * - 对象构建过程清晰，减少错误
 * - 便于扩展新的事件属性
 * 
 * 数据完整性：
 * - 自动生成唯一事件ID
 * - 记录详细的用户和环境信息
 * - 支持事件严重性级别管理
 * - 提供结构化的事件详情存储
 *
 * @author lx
 * @date 2025/06/08
 */
public class SecurityEvent {
    
    /** 安全事件唯一标识符，自动生成UUID确保事件可追溯 */
    private String eventId;
    
    /** 安全事件类型，枚举值，标识具体的安全事件分类 */
    private SecurityEventType eventType;
    
    /** 用户ID，关联触发安全事件的用户身份 */
    private String userId;
    
    /** 用户名，便于安全事件的可读性显示和人工分析 */
    private String username;
    
    /** 源IP地址，记录安全事件发生的网络位置 */
    private String sourceIp;
    
    /** 用户代理字符串，包含客户端浏览器和操作系统信息 */
    private String userAgent;
    
    /** 安全事件严重程度，用于事件优先级排序和响应策略 */
    private SecuritySeverity severity;
    
    /** 安全事件描述信息，提供事件的详细说明 */
    private String message;
    
    /** 安全事件发生时间，精确记录事件时间戳 */
    private LocalDateTime timestamp;
    
    /** 安全事件详细信息，存储事件相关的扩展数据和上下文 */
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