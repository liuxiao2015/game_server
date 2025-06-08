package com.game.frame.netty.session;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Session manager providing session creation, query, deletion, and broadcast functionality
 *
 * @author lx
 * @date 2024-01-01
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<>();
    
    /**
     * Creates a new session for the given channel
     * 
     * @param channel the Netty channel
     * @return created session
     */
    public Session createSession(Channel channel) {
        Session session = new Session(channel);
        sessions.put(session.getSessionId(), session);
        
        logger.debug("Session created: {}", session.getSessionId());
        return session;
    }
    
    /**
     * Gets a session by session ID
     * 
     * @param sessionId session ID
     * @return session or null if not found
     */
    public Session getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    /**
     * Gets a session by user ID
     * 
     * @param userId user ID
     * @return session or null if not found
     */
    public Session getSessionByUserId(String userId) {
        return userSessions.get(userId);
    }
    
    /**
     * Gets a session by channel
     * 
     * @param channel the Netty channel
     * @return session or null if not found
     */
    public Session getSession(Channel channel) {
        return sessions.values().stream()
                .filter(session -> session.getChannel() == channel)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Binds a user ID to a session
     * 
     * @param sessionId session ID
     * @param userId user ID
     * @return true if binding successful
     */
    public boolean bindUser(String sessionId, String userId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            logger.warn("Session not found for binding: {}", sessionId);
            return false;
        }
        
        // Remove old binding if exists
        if (session.getUserId() != null) {
            userSessions.remove(session.getUserId());
        }
        
        // Check if user is already bound to another session
        Session existingSession = userSessions.get(userId);
        if (existingSession != null && !existingSession.getSessionId().equals(sessionId)) {
            logger.warn("User {} already bound to another session: {}", userId, existingSession.getSessionId());
            // Close the old session
            removeSession(existingSession.getSessionId());
        }
        
        session.setUserId(userId);
        session.setAuthenticated(true);
        userSessions.put(userId, session);
        
        logger.info("User {} bound to session {}", userId, sessionId);
        return true;
    }
    
    /**
     * Removes a session by session ID
     * 
     * @param sessionId session ID
     * @return removed session or null
     */
    public Session removeSession(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session != null) {
            // Remove user binding
            if (session.getUserId() != null) {
                userSessions.remove(session.getUserId());
            }
            
            // Close the channel
            session.close();
            
            logger.debug("Session removed: {}", sessionId);
        }
        return session;
    }
    
    /**
     * Removes a session by channel
     * 
     * @param channel the Netty channel
     * @return removed session or null
     */
    public Session removeSession(Channel channel) {
        Session session = getSession(channel);
        if (session != null) {
            return removeSession(session.getSessionId());
        }
        return null;
    }
    
    /**
     * Gets all active sessions
     * 
     * @return collection of all sessions
     */
    public Collection<Session> getAllSessions() {
        return sessions.values();
    }
    
    /**
     * Gets the number of active sessions
     * 
     * @return session count
     */
    public int getSessionCount() {
        return sessions.size();
    }
    
    /**
     * Gets the number of authenticated sessions
     * 
     * @return authenticated session count
     */
    public int getAuthenticatedSessionCount() {
        return userSessions.size();
    }
    
    /**
     * Broadcasts a message to all sessions
     * 
     * @param message the message to broadcast
     */
    public void broadcastToAll(Object message) {
        sessions.values().forEach(session -> {
            try {
                session.sendMessage(message);
            } catch (Exception e) {
                logger.error("Failed to send message to session: {}", session.getSessionId(), e);
            }
        });
        
        logger.debug("Broadcast message to {} sessions", sessions.size());
    }
    
    /**
     * Broadcasts a message to authenticated sessions
     * 
     * @param message the message to broadcast
     */
    public void broadcastToAuthenticated(Object message) {
        userSessions.values().forEach(session -> {
            try {
                session.sendMessage(message);
            } catch (Exception e) {
                logger.error("Failed to send message to session: {}", session.getSessionId(), e);
            }
        });
        
        logger.debug("Broadcast message to {} authenticated sessions", userSessions.size());
    }
    
    /**
     * Broadcasts a message to sessions matching the predicate
     * 
     * @param message the message to broadcast
     * @param predicate session filter predicate
     */
    public void broadcastTo(Object message, Predicate<Session> predicate) {
        int count = 0;
        for (Session session : sessions.values()) {
            if (predicate.test(session)) {
                try {
                    session.sendMessage(message);
                    count++;
                } catch (Exception e) {
                    logger.error("Failed to send message to session: {}", session.getSessionId(), e);
                }
            }
        }
        
        logger.debug("Broadcast message to {} filtered sessions", count);
    }
    
    /**
     * Removes inactive sessions based on timeout
     * 
     * @param timeoutMs timeout in milliseconds
     * @return number of removed sessions
     */
    public int removeInactiveSessions(long timeoutMs) {
        long currentTime = System.currentTimeMillis();
        int[] removedCount = {0}; // Use array to make it effectively final
        
        sessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (currentTime - session.getLastActiveTime() > timeoutMs || !session.isActive()) {
                // Remove user binding
                if (session.getUserId() != null) {
                    userSessions.remove(session.getUserId());
                }
                session.close();
                removedCount[0]++;
                logger.debug("Removed inactive session: {}", session.getSessionId());
                return true;
            }
            return false;
        });
        
        if (removedCount[0] > 0) {
            logger.info("Removed {} inactive sessions", removedCount[0]);
        }
        
        return removedCount[0];
    }
    
    /**
     * Clears all sessions
     */
    public void clear() {
        sessions.values().forEach(Session::close);
        sessions.clear();
        userSessions.clear();
        logger.info("All sessions cleared");
    }
}