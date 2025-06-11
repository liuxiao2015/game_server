package com.game.frame.netty.session;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serializable session data for Redis storage
 * 
 * 功能说明：
 * - 存储可序列化的会话信息，用于Redis分布式存储
 * - 不包含Channel等不可序列化的对象，专注于数据存储
 * - 提供会话基本信息的访问和修改功能
 * - 支持自定义属性的存储和管理
 *
 * 设计考虑：
 * - 与Session类分离，避免序列化问题
 * - 包含会话管理所需的核心数据
 * - 支持与Session对象的相互转换
 * - 优化存储空间和网络传输效率
 *
 * @author lx
 * @date 2024-01-01
 */
public class SessionData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String sessionId;
    private String userId;
    private boolean authenticated = false;
    private long createTime;
    private long lastActiveTime;
    private String remoteAddress;
    private ConcurrentHashMap<String, Object> attributes;
    
    public SessionData() {
        // 默认构造函数
    }
    
    /**
     * 从Session对象创建SessionData
     */
    public SessionData(Session session) {
        if (session != null) {
            this.sessionId = session.getSessionId();
            this.userId = session.getUserId();
            this.authenticated = session.isAuthenticated();
            this.createTime = session.getCreateTime();
            this.lastActiveTime = session.getLastActiveTime();
            this.remoteAddress = session.getRemoteAddress();
            this.attributes = new ConcurrentHashMap<>(session.getAttributes());
        }
    }
    
    // Getters and Setters
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public long getLastActiveTime() {
        return lastActiveTime;
    }
    
    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }
    
    public String getRemoteAddress() {
        return remoteAddress;
    }
    
    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    
    public ConcurrentHashMap<String, Object> getAttributes() {
        if (attributes == null) {
            attributes = new ConcurrentHashMap<>();
        }
        return attributes;
    }
    
    public void setAttributes(ConcurrentHashMap<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    /**
     * 检查会话是否过期
     */
    public boolean isExpired() {
        return isExpired(7200 * 1000L); // 默认2小时过期
    }
    
    /**
     * 检查会话是否过期
     * 
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否过期
     */
    public boolean isExpired(long timeoutMs) {
        return System.currentTimeMillis() - lastActiveTime > timeoutMs;
    }
    
    @Override
    public String toString() {
        return "SessionData{" +
                "sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                ", authenticated=" + authenticated +
                ", createTime=" + createTime +
                ", lastActiveTime=" + lastActiveTime +
                ", remoteAddress='" + remoteAddress + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionData that = (SessionData) o;
        return sessionId != null ? sessionId.equals(that.sessionId) : that.sessionId == null;
    }
    
    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }
}