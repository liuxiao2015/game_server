package com.game.frame.netty.session;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Session abstraction containing channel reference, session ID, user info, 
 * last active time, and custom attributes
 *
 * @author lx
 * @date 2024-01-01
 */
public class Session {
    
    private static final AtomicLong SESSION_ID_GENERATOR = new AtomicLong(1);
    
    private final String sessionId;
    private final Channel channel;
    private final long createTime;
    private volatile long lastActiveTime;
    private volatile String userId;
    private volatile boolean authenticated = false;
    private final ConcurrentHashMap<String, Object> attributes;
    
    /**
     * Creates a new session
     * 
     * @param channel the Netty channel
     */
    public Session(Channel channel) {
        this.sessionId = "SESSION-" + SESSION_ID_GENERATOR.getAndIncrement();
        this.channel = channel;
        this.createTime = System.currentTimeMillis();
        this.lastActiveTime = createTime;
        this.attributes = new ConcurrentHashMap<>();
    }
    
    /**
     * Gets the session ID
     * 
     * @return session ID
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Gets the Netty channel
     * 
     * @return channel
     */
    public Channel getChannel() {
        return channel;
    }
    
    /**
     * Gets the creation time
     * 
     * @return creation timestamp
     */
    public long getCreateTime() {
        return createTime;
    }
    
    /**
     * Gets the last active time
     * 
     * @return last active timestamp
     */
    public long getLastActiveTime() {
        return lastActiveTime;
    }
    
    /**
     * Updates the last active time to now
     */
    public void updateActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }
    
    /**
     * Gets the user ID
     * 
     * @return user ID or null if not set
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * Sets the user ID
     * 
     * @param userId user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     * Checks if the session is authenticated
     * 
     * @return true if authenticated
     */
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    /**
     * Sets the authentication status
     * 
     * @param authenticated authentication status
     */
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    /**
     * Gets a custom attribute
     * 
     * @param key attribute key
     * @param <T> attribute type
     * @return attribute value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }
    
    /**
     * Sets a custom attribute
     * 
     * @param key attribute key
     * @param value attribute value
     * @param <T> attribute type
     * @return previous value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T setAttribute(String key, Object value) {
        return (T) attributes.put(key, value);
    }
    
    /**
     * Removes a custom attribute
     * 
     * @param key attribute key
     * @param <T> attribute type
     * @return removed value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T removeAttribute(String key) {
        return (T) attributes.remove(key);
    }
    
    /**
     * Checks if the channel is active and writable
     * 
     * @return true if channel is active and writable
     */
    public boolean isActive() {
        return channel != null && channel.isActive() && channel.isWritable();
    }
    
    /**
     * Sends a message through the channel
     * 
     * @param message the message to send
     */
    public void sendMessage(Object message) {
        if (isActive()) {
            channel.writeAndFlush(message);
            updateActiveTime();
        }
    }
    
    /**
     * Closes the session and channel
     */
    public void close() {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }
    
    /**
     * Gets the remote address as string
     * 
     * @return remote address or "unknown"
     */
    public String getRemoteAddress() {
        if (channel != null && channel.remoteAddress() != null) {
            return channel.remoteAddress().toString();
        }
        return "unknown";
    }
    
    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                ", authenticated=" + authenticated +
                ", createTime=" + createTime +
                ", lastActiveTime=" + lastActiveTime +
                ", remoteAddress='" + getRemoteAddress() + '\'' +
                ", active=" + isActive() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return sessionId.equals(session.sessionId);
    }
    
    @Override
    public int hashCode() {
        return sessionId.hashCode();
    }
}