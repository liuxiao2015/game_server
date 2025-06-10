package com.game.service.chat.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket session manager for chat service
 * Manages WebSocket sessions and user mappings
 *
 * @author lx
 * @date 2025/01/08
 */
@Component
/**
 * WebSocketSession管理器
 * 
 * 功能说明：
 * - 管理特定资源或组件的生命周期
 * - 提供统一的操作接口和控制逻辑
 * - 协调多个组件的协作关系
 *
 * @author lx
 * @date 2024-01-01
 */
public class WebSocketSessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionManager.class);
    
    // User ID to WebSocket session mapping
    private final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    // Session ID to User ID mapping
    private final ConcurrentHashMap<String, Long> sessionToUser = new ConcurrentHashMap<>();
    
    // Last activity tracking
    private final ConcurrentHashMap<Long, Long> lastActivity = new ConcurrentHashMap<>();

    /**
     * Add WebSocket session for user
     */
    public void addSession(Long userId, WebSocketSession session) {
        // Remove existing session if any
        removeSession(userId);
        
        userSessions.put(userId, session);
        sessionToUser.put(session.getId(), userId);
        updateActivity(userId);
        
        logger.info("Added WebSocket session for user {}: {}", userId, session.getId());
    }

    /**
     * Remove WebSocket session for user
     */
    public void removeSession(Long userId) {
        WebSocketSession session = userSessions.remove(userId);
        if (session != null) {
            sessionToUser.remove(session.getId());
            lastActivity.remove(userId);
            logger.info("Removed WebSocket session for user {}: {}", userId, session.getId());
        }
    }

    /**
     * Get WebSocket session for user
     */
    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }

    /**
     * Get user ID by WebSocket session
     */
    public Long getUserId(WebSocketSession session) {
        return sessionToUser.get(session.getId());
    }

    /**
     * Check if user is online
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * Update user activity timestamp
     */
    public void updateActivity(Long userId) {
        lastActivity.put(userId, System.currentTimeMillis());
    }

    /**
     * Get last activity timestamp for user
     */
    public Long getLastActivity(Long userId) {
        return lastActivity.get(userId);
    }

    /**
     * Get all online users
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * Broadcast message to all online users
     */
    public void broadcastToAll(Object message) {
        userSessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    // Implementation would depend on message format
                    // This is a placeholder for broadcast functionality
                }
            } catch (Exception e) {
                logger.error("Error broadcasting to session {}: {}", session.getId(), e.getMessage(), e);
            }
        });
    }

    /**
     * Send message to specific user
     */
    public boolean sendToUser(Long userId, Object message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                // Implementation would depend on message format
                // This is a placeholder for user-specific messaging
                return true;
            } catch (Exception e) {
                logger.error("Error sending message to user {}: {}", userId, e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * Clean up inactive sessions
     */
    public void cleanupInactiveSessions(long timeoutMs) {
        long currentTime = System.currentTimeMillis();
        
        lastActivity.entrySet().removeIf(entry -> {
            Long userId = entry.getKey();
            Long lastActivityTime = entry.getValue();
            
            if (currentTime - lastActivityTime > timeoutMs) {
                logger.info("Removing inactive user session: {}", userId);
                removeSession(userId);
                return true;
            }
            return false;
        });
    }
}