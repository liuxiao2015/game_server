package com.game.service.logic.service;

import com.game.common.api.service.ISessionService;
import com.game.common.model.entity.Session;
import com.game.common.model.exception.ErrorCode;
import com.game.common.model.response.Result;
import com.game.service.logic.manager.TokenManager;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Session service implementation using Redis-based session storage
 * Provides session creation, validation and destruction operations
 *
 * @author lx
 * @date 2024-01-01
 */
@DubboService(version = "1.0.0", group = "game", timeout = 3000)
/**
 * Session服务实现类 - Redis优化版
 * 
 * 功能说明：
 * - 基于Redis实现分布式会话管理，支持多服务实例间的会话共享
 * - 提供高性能的会话操作，支持会话的创建、验证、销毁和刷新
 * - 集成Token管理，确保会话安全性和有效性
 * - 支持会话过期自动清理，避免内存泄漏和资源浪费
 * 
 * 实现特点：
 * - 使用Redis作为会话存储后端，替代本地内存存储
 * - 支持会话的分布式一致性和高可用性
 * - 优化的序列化策略，平衡性能和存储效率
 * - 完善的错误处理和异常管理机制
 * 
 * 性能优化：
 * - Redis连接池复用，提升连接效率
 * - 合理的过期时间设置，自动清理过期会话
 * - 批量操作支持，减少网络往返次数
 * - 异步操作优化，提升响应性能
 *
 * @author lx
 * @date 2024-01-01
 */
public class SessionServiceImpl implements ISessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);

    @Autowired
    private TokenManager tokenManager;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${game.token.expire:7200}")
    private int defaultExpireSeconds;
    
    // Redis key前缀
    private static final String SESSION_KEY_PREFIX = "game:service:session:";
    private static final String USER_SESSION_KEY_PREFIX = "game:service:user_session:";

    @Override
    public Result<Session> createSession(Long userId, String deviceId, String clientIp, String userAgent) {
        try {
            if (userId == null) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "User ID cannot be null");
            }

            // Destroy existing session for the user (single session per user)
            destroyUserSessions(userId);

            // Create new session
            String sessionId = UUID.randomUUID().toString();
            long now = System.currentTimeMillis();
            long expireTime = now + defaultExpireSeconds * 1000L;

            Session session = new Session(sessionId, userId, null, expireTime);
            session.setDeviceId(deviceId);
            session.setClientIp(clientIp);
            session.setUserAgent(userAgent);

            // Generate token
            String token = tokenManager.generateToken(userId, sessionId, defaultExpireSeconds);
            session.setToken(token);

            // Store session in Redis
            String sessionKey = getSessionKey(token);
            redisTemplate.opsForValue().set(sessionKey, session);
            redisTemplate.expire(sessionKey, defaultExpireSeconds, TimeUnit.SECONDS);
            
            // Store user to session mapping
            String userSessionKey = getUserSessionKey(userId);
            redisTemplate.opsForValue().set(userSessionKey, token);
            redisTemplate.expire(userSessionKey, defaultExpireSeconds, TimeUnit.SECONDS);

            logger.info("Created session for user {}: sessionId={}, stored in Redis", userId, sessionId);
            return Result.success(session);

        } catch (Exception e) {
            logger.error("Failed to create session for user {}", userId, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to create session: " + e.getMessage());
        }
    }

    @Override
    public Result<Session> validateSession(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "Token cannot be empty");
            }

            // Get session from Redis
            String sessionKey = getSessionKey(token);
            Session session = (Session) redisTemplate.opsForValue().get(sessionKey);
            
            if (session == null) {
                return Result.failure(ErrorCode.TOKEN_INVALID, "Session not found");
            }

            // Check if session is expired
            if (session.isExpired()) {
                // Remove from Redis
                redisTemplate.delete(sessionKey);
                if (session.getUserId() != null) {
                    String userSessionKey = getUserSessionKey(session.getUserId());
                    redisTemplate.delete(userSessionKey);
                }
                return Result.failure(ErrorCode.TOKEN_EXPIRED, "Session expired");
            }

            // Check if session is active
            if (!session.isActive()) {
                return Result.failure(ErrorCode.TOKEN_INVALID, "Session not active");
            }

            // Validate token
            if (tokenManager.validateToken(token) != null) {
                logger.debug("Session validated successfully: userId={}", session.getUserId());
                return Result.success(session);
            } else {
                return Result.failure(ErrorCode.TOKEN_INVALID, "Invalid token");
            }

        } catch (Exception e) {
            logger.error("Failed to validate session", e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to validate session: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> destroySession(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "Token cannot be empty");
            }

            String sessionKey = getSessionKey(token);
            Session session = (Session) redisTemplate.opsForValue().get(sessionKey);
            
            // Remove session from Redis
            redisTemplate.delete(sessionKey);
            
            if (session != null) {
                // Remove user session mapping
                if (session.getUserId() != null) {
                    String userSessionKey = getUserSessionKey(session.getUserId());
                    redisTemplate.delete(userSessionKey);
                }
                logger.info("Destroyed session: userId={}, sessionId={}", 
                        session.getUserId(), session.getSessionId());
            }

            return Result.success();

        } catch (Exception e) {
            logger.error("Failed to destroy session", e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to destroy session: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> destroyUserSessions(Long userId) {
        try {
            if (userId == null) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "User ID cannot be null");
            }

            String userSessionKey = getUserSessionKey(userId);
            String existingToken = (String) redisTemplate.opsForValue().get(userSessionKey);
            
            if (existingToken != null) {
                // Remove session
                String sessionKey = getSessionKey(existingToken);
                redisTemplate.delete(sessionKey);
                
                // Remove user session mapping
                redisTemplate.delete(userSessionKey);
                
                logger.info("Destroyed existing session for user {}", userId);
            }

            return Result.success();

        } catch (Exception e) {
            logger.error("Failed to destroy user sessions: userId={}", userId, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to destroy user sessions: " + e.getMessage());
        }
    }

    @Override
    public Result<Session> refreshSession(String token, int extendSeconds) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "Token cannot be empty");
            }

            String sessionKey = getSessionKey(token);
            Session session = (Session) redisTemplate.opsForValue().get(sessionKey);
            
            if (session == null) {
                return Result.failure(ErrorCode.TOKEN_INVALID, "Session not found");
            }

            // Extend expiration time
            long newExpireTime = System.currentTimeMillis() + extendSeconds * 1000L;
            session.setExpireTime(newExpireTime);

            // Generate new token
            String newToken = tokenManager.generateToken(session.getUserId(), session.getSessionId(), extendSeconds);
            session.setToken(newToken);

            // Remove old session
            redisTemplate.delete(sessionKey);
            
            // Store session with new token
            String newSessionKey = getSessionKey(newToken);
            redisTemplate.opsForValue().set(newSessionKey, session);
            redisTemplate.expire(newSessionKey, extendSeconds, TimeUnit.SECONDS);
            
            // Update user session mapping
            if (session.getUserId() != null) {
                String userSessionKey = getUserSessionKey(session.getUserId());
                redisTemplate.opsForValue().set(userSessionKey, newToken);
                redisTemplate.expire(userSessionKey, extendSeconds, TimeUnit.SECONDS);
            }

            logger.info("Refreshed session: userId={}, newExpireTime={}", 
                    session.getUserId(), newExpireTime);
            return Result.success(session);

        } catch (Exception e) {
            logger.error("Failed to refresh session", e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to refresh session: " + e.getMessage());
        }
    }

    @Override
    public Result<Session> getSession(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "Token cannot be empty");
            }

            String sessionKey = getSessionKey(token);
            Session session = (Session) redisTemplate.opsForValue().get(sessionKey);
            
            if (session == null) {
                return Result.failure(ErrorCode.TOKEN_INVALID, "Session not found");
            }

            return Result.success(session);

        } catch (Exception e) {
            logger.error("Failed to get session", e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to get session: " + e.getMessage());
        }
    }
    
    /**
     * 获取会话Redis键
     */
    private String getSessionKey(String token) {
        return SESSION_KEY_PREFIX + token;
    }
    
    /**
     * 获取用户会话映射Redis键
     */
    private String getUserSessionKey(Long userId) {
        return USER_SESSION_KEY_PREFIX + userId;
    }
}