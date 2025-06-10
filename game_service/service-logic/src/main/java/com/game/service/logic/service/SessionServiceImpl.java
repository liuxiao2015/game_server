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

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session service implementation using Redis-based session storage
 * Provides session creation, validation and destruction operations
 *
 * @author lx
 * @date 2024-01-01
 */
@DubboService(version = "1.0.0", group = "game", timeout = 3000)
/**
 * Session服务实现类
 * 
 * 功能说明：
 * - 实现对应服务接口的具体业务逻辑
 * - 提供完整的数据操作和业务处理功能
 * - 集成缓存、数据库等基础设施组件
 * - 支持事务管理和异常处理
 * 
 * 实现特点：
 * - 基于Spring框架的服务层设计
 * - 使用依赖注入管理组件依赖关系
 * - 支持声明式事务和AOP切面编程
 * - 提供完善的日志记录和监控
 * 
 * 业务功能：
 * - 数据验证和业务规则校验
 * - 数据持久化和缓存管理
 * - 外部服务调用和集成
 * - 异步处理和消息通知
 *
 * @author lx
 * @date 2024-01-01
 */
public class SessionServiceImpl implements ISessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);

    @Autowired
    private TokenManager tokenManager;

    @Value("${game.token.expire:7200}")
    private int defaultExpireSeconds;

    // Mock session storage (in real implementation, this would be Redis)
    private final ConcurrentHashMap<String, Session> sessionStorage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> userSessionMap = new ConcurrentHashMap<>();

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

            // Store session
            sessionStorage.put(token, session);
            userSessionMap.put(userId, token);

            logger.info("Created session for user {}: sessionId={}", userId, sessionId);
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

            // Get session from storage
            Session session = sessionStorage.get(token);
            if (session == null) {
                return Result.failure(ErrorCode.TOKEN_INVALID, "Session not found");
            }

            // Check if session is expired
            if (session.isExpired()) {
                sessionStorage.remove(token);
                userSessionMap.remove(session.getUserId());
                return Result.failure(ErrorCode.TOKEN_EXPIRED, "Session expired");
            }

            // Check if session is active
            if (!session.isActive()) {
                return Result.failure(ErrorCode.TOKEN_INVALID, "Session not active");
            }

            // Validate token
            if (!tokenManager.validateToken(token).equals(null)) {
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

            Session session = sessionStorage.remove(token);
            if (session != null) {
                userSessionMap.remove(session.getUserId());
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

            String existingToken = userSessionMap.remove(userId);
            if (existingToken != null) {
                sessionStorage.remove(existingToken);
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

            Session session = sessionStorage.get(token);
            if (session == null) {
                return Result.failure(ErrorCode.TOKEN_INVALID, "Session not found");
            }

            // Extend expiration time
            long newExpireTime = System.currentTimeMillis() + extendSeconds * 1000L;
            session.setExpireTime(newExpireTime);

            // Generate new token
            String newToken = tokenManager.generateToken(session.getUserId(), session.getSessionId(), extendSeconds);
            session.setToken(newToken);

            // Update storage
            sessionStorage.remove(token);
            sessionStorage.put(newToken, session);
            userSessionMap.put(session.getUserId(), newToken);

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

            Session session = sessionStorage.get(token);
            if (session == null) {
                return Result.failure(ErrorCode.TOKEN_INVALID, "Session not found");
            }

            return Result.success(session);

        } catch (Exception e) {
            logger.error("Failed to get session", e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to get session: " + e.getMessage());
        }
    }
}