package com.game.common.api.service;

import com.game.common.model.entity.Session;
import com.game.common.model.response.Result;

/**
 * Session service interface
 * Provides session creation, validation and destruction operations
 *
 * @author lx
 * @date 2024-01-01
 */
public interface ISessionService {

    /**
     * Create a new session
     *
     * @param userId user ID
     * @param deviceId device ID
     * @param clientIp client IP address
     * @param userAgent user agent
     * @return session with token
     */
    Result<Session> createSession(Long userId, String deviceId, String clientIp, String userAgent);

    /**
     * Validate session by token
     *
     * @param token session token
     * @return session if valid, null if invalid or expired
     */
    Result<Session> validateSession(String token);

    /**
     * Destroy session by token
     *
     * @param token session token
     * @return destroy result
     */
    Result<Void> destroySession(String token);

    /**
     * Destroy all sessions for a user
     *
     * @param userId user ID
     * @return destroy result
     */
    Result<Void> destroyUserSessions(Long userId);

    /**
     * Refresh session expiration time
     *
     * @param token session token
     * @param extendSeconds seconds to extend
     * @return refresh result
     */
    Result<Session> refreshSession(String token, int extendSeconds);

    /**
     * Get session by token
     *
     * @param token session token
     * @return session entity
     */
    Result<Session> getSession(String token);
}