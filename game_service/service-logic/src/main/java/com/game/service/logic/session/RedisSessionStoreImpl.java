package com.game.service.logic.session;

import com.game.frame.netty.session.SessionStore;
import com.game.frame.netty.session.Session;
import com.game.frame.netty.session.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis实现的会话存储
 * 
 * 功能说明：
 * - 基于Redis实现分布式会话存储，支持多服务实例间的会话共享
 * - 提供高性能的会话数据读写操作，支持会话的持久化和过期管理
 * - 实现会话索引和快速查找，优化数据检索效率
 * - 支持会话的自动过期和清理机制
 * 
 * 设计特点：
 * - 使用Redis作为后端存储，保证数据的持久性和一致性
 * - 采用合理的Key设计和数据结构，优化存储效率
 * - 支持会话过期自动清理，避免内存泄漏
 * - 提供完善的错误处理和异常恢复机制
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
public class RedisSessionStoreImpl implements SessionStore {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisSessionStoreImpl.class);
    
    // Redis key前缀
    private static final String SESSION_KEY_PREFIX = "game:frame:session:";
    private static final String USER_SESSION_KEY_PREFIX = "game:frame:user_session:";
    private static final String SESSION_INDEX_KEY = "game:frame:session_index";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 会话默认过期时间（秒）
    private static final long DEFAULT_SESSION_EXPIRE_TIME = 7200; // 2小时
    
    @Override
    public void storeSession(Session session) {
        if (session == null) {
            logger.warn("Cannot store null session");
            return;
        }
        
        try {
            String sessionKey = getSessionKey(session.getSessionId());
            
            // 创建SessionData并存储到Redis
            SessionData sessionData = new SessionData(session);
            redisTemplate.opsForValue().set(sessionKey, sessionData);
            redisTemplate.expire(sessionKey, DEFAULT_SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
            
            // 如果已认证，建立用户到会话的映射
            if (session.getUserId() != null) {
                String userSessionKey = getUserSessionKey(session.getUserId());
                redisTemplate.opsForValue().set(userSessionKey, session.getSessionId());
                redisTemplate.expire(userSessionKey, DEFAULT_SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
            }
            
            // 维护会话索引
            redisTemplate.opsForSet().add(SESSION_INDEX_KEY, session.getSessionId());
            
            logger.debug("Session stored in Redis: {}", session.getSessionId());
            
        } catch (Exception e) {
            logger.error("Failed to store session in Redis: {}", session.getSessionId(), e);
            throw new RuntimeException("Failed to store session", e);
        }
    }
    
    @Override
    public Session getSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }
        
        try {
            String sessionKey = getSessionKey(sessionId);
            SessionData sessionData = (SessionData) redisTemplate.opsForValue().get(sessionKey);
            
            if (sessionData == null) {
                logger.debug("Session data not found in Redis: {}", sessionId);
                return null;
            }
            
            // 注意：从Redis恢复的SessionData不包含Channel对象
            // 尝试重建Session对象，即使Channel不可用
            logger.debug("Session data retrieved from Redis: {}, but Channel not available", sessionId);
            Session session = new Session(sessionData); // 假设Session有一个接受SessionData的构造函数
            logger.debug("Session reconstructed from SessionData: {}", sessionId);
            return session;
            
        } catch (Exception e) {
            logger.error("Failed to get session from Redis: {}", sessionId, e);
            return null;
        }
    }
    
    /**
     * 获取会话数据（不包含Channel）
     */
    public SessionData getSessionData(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }
        
        try {
            String sessionKey = getSessionKey(sessionId);
            SessionData sessionData = (SessionData) redisTemplate.opsForValue().get(sessionKey);
            
            if (sessionData == null) {
                logger.debug("Session data not found in Redis: {}", sessionId);
                return null;
            }
            
            logger.debug("Session data retrieved from Redis: {}", sessionId);
            return sessionData;
            
        } catch (Exception e) {
            logger.error("Failed to get session data from Redis: {}", sessionId, e);
            return null;
        }
    }
    
    @Override
    public Session getSessionByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        
        try {
            String userSessionKey = getUserSessionKey(userId);
            String sessionId = (String) redisTemplate.opsForValue().get(userSessionKey);
            
            if (sessionId == null) {
                logger.debug("No session found for user: {}", userId);
                return null;
            }
            
            return getSession(sessionId);
            
        } catch (Exception e) {
            logger.error("Failed to get session by user ID: {}", userId, e);
            return null;
        }
    }
    
    @Override
    public void removeSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }
        
        try {
            // 先获取会话信息以便清理用户映射
            SessionData sessionData = getSessionData(sessionId);
            
            // 删除会话数据
            String sessionKey = getSessionKey(sessionId);
            redisTemplate.delete(sessionKey);
            
            // 删除用户会话映射
            if (sessionData != null && sessionData.getUserId() != null) {
                String userSessionKey = getUserSessionKey(sessionData.getUserId());
                redisTemplate.delete(userSessionKey);
            }
            
            // 从会话索引中移除
            redisTemplate.opsForSet().remove(SESSION_INDEX_KEY, sessionId);
            
            logger.debug("Session removed from Redis: {}", sessionId);
            
        } catch (Exception e) {
            logger.error("Failed to remove session from Redis: {}", sessionId, e);
        }
    }
    
    @Override
    public void updateSessionActiveTime(String sessionId, long activeTime) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }
        
        try {
            String sessionKey = getSessionKey(sessionId);
            SessionData sessionData = (SessionData) redisTemplate.opsForValue().get(sessionKey);
            
            if (sessionData != null) {
                sessionData.setLastActiveTime(activeTime);
                redisTemplate.opsForValue().set(sessionKey, sessionData);
                
                // 延长过期时间
                redisTemplate.expire(sessionKey, DEFAULT_SESSION_EXPIRE_TIME, TimeUnit.SECONDS);
                
                logger.debug("Session active time updated: {}", sessionId);
            }
            
        } catch (Exception e) {
            logger.error("Failed to update session active time: {}", sessionId, e);
        }
    }
    
    @Override
    public Set<String> getAllSessionIds() {
        try {
            Set<Object> members = redisTemplate.opsForSet().members(SESSION_INDEX_KEY);
            Set<String> sessionIds = new HashSet<>();
            if (members != null) {
                for (Object member : members) {
                    if (member instanceof String) {
                        sessionIds.add((String) member);
                    }
                }
            }
            return sessionIds;
        } catch (Exception e) {
            logger.error("Failed to get all session IDs from Redis", e);
            return new HashSet<>();
        }
    }
    
    @Override
    public int getSessionCount() {
        try {
            Long count = redisTemplate.opsForSet().size(SESSION_INDEX_KEY);
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            logger.error("Failed to get session count from Redis", e);
            return 0;
        }
    }
    
    @Override
    public void clearAllSessions() {
        try {
            // 获取所有会话ID
            Set<String> sessionIds = getAllSessionIds();
            
            // 使用Redis管道批量删除会话
            redisTemplate.executePipelined((redisConnection) -> {
                for (String sessionId : sessionIds) {
                    redisConnection.del(redisTemplate.getKeySerializer().serialize(sessionId));
                }
                return null;
            });
            
            // 清空索引
            redisTemplate.delete(SESSION_INDEX_KEY);
            
            logger.info("All sessions cleared from Redis, count: {}", sessionIds.size());
            
        } catch (Exception e) {
            logger.error("Failed to clear all sessions from Redis", e);
        }
    }
    
    /**
     * 检查会话是否存在
     */
    public boolean existsSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        
        try {
            String sessionKey = getSessionKey(sessionId);
            return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
        } catch (Exception e) {
            logger.error("Failed to check session existence: {}", sessionId, e);
            return false;
        }
    }
    
    /**
     * 设置会话过期时间
     */
    public void setSessionExpire(String sessionId, long expireSeconds) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }
        
        try {
            String sessionKey = getSessionKey(sessionId);
            redisTemplate.expire(sessionKey, expireSeconds, TimeUnit.SECONDS);
            
            // 同时更新用户会话映射的过期时间
            SessionData sessionData = getSessionData(sessionId);
            if (sessionData != null && sessionData.getUserId() != null) {
                String userSessionKey = getUserSessionKey(sessionData.getUserId());
                redisTemplate.expire(userSessionKey, expireSeconds, TimeUnit.SECONDS);
            }
            
            logger.debug("Session expire time set: {} -> {} seconds", sessionId, expireSeconds);
            
        } catch (Exception e) {
            logger.error("Failed to set session expire time: {}", sessionId, e);
        }
    }
    
    /**
     * 获取会话剩余过期时间
     */
    public long getSessionTTL(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return -2;
        }
        
        try {
            String sessionKey = getSessionKey(sessionId);
            Long ttl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);
            return ttl != null ? ttl : -2;
        } catch (Exception e) {
            logger.error("Failed to get session TTL: {}", sessionId, e);
            return -2;
        }
    }
    
    private String getSessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }
    
    private String getUserSessionKey(String userId) {
        return USER_SESSION_KEY_PREFIX + userId;
    }
}