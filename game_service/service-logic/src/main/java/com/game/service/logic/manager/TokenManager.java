package com.game.service.logic.manager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Token manager for JWT generation, validation and refresh
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
/**
 * Token管理器
 * 
 * 功能说明：
 * - 管理特定资源或组件的生命周期
 * - 提供统一的操作接口和控制逻辑
 * - 协调多个组件的协作关系
 *
 * @author lx
 * @date 2024-01-01
 */
public class TokenManager {

    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);

    @Value("${game.token.secret}")
    private String tokenSecret;

    @Value("${game.token.expire}")
    private int tokenExpireSeconds;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(tokenSecret.getBytes());
    }

    /**
     * Generate JWT token
     *
     * @param userId user ID
     * @param sessionId session ID
     * @return JWT token
     */
    public String generateToken(Long userId, String sessionId) {
        return generateToken(userId, sessionId, tokenExpireSeconds);
    }

    /**
     * Generate JWT token with custom expire time
     *
     * @param userId user ID
     * @param sessionId session ID
     * @param expireSeconds expire time in seconds
     * @return JWT token
     */
    public String generateToken(Long userId, String sessionId, int expireSeconds) {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + expireSeconds * 1000L);

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userId);
            claims.put("sessionId", sessionId);
            claims.put("issuedAt", now.getTime());

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userId.toString())
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact();

            logger.debug("Generated token for user {} with session {}", userId, sessionId);
            return token;
        } catch (Exception e) {
            logger.error("Failed to generate token for user {}", userId, e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    /**
     * Validate and parse JWT token
     *
     * @param token JWT token
     * @return token claims if valid, null if invalid
     */
    public Claims validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            logger.debug("Token validated successfully");
            return claims;
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract user ID from token
     *
     * @param token JWT token
     * @return user ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
        }
        return null;
    }

    /**
     * Extract session ID from token
     *
     * @param token JWT token
     * @return session ID
     */
    public String getSessionIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            return (String) claims.get("sessionId");
        }
        return null;
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token
     * @return true if expired
     */
    public boolean isTokenExpired(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            return claims.getExpiration().before(new Date());
        }
        return true;
    }

    /**
     * Refresh token with new expiration time
     *
     * @param token original token
     * @return new token with extended expiration
     */
    public String refreshToken(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            Long userId = getUserIdFromToken(token);
            String sessionId = getSessionIdFromToken(token);
            if (userId != null && sessionId != null) {
                return generateToken(userId, sessionId);
            }
        }
        return null;
    }

    /**
     * Get token expiration time
     *
     * @param token JWT token
     * @return expiration time in milliseconds
     */
    public Long getTokenExpiration(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            return claims.getExpiration().getTime();
        }
        return null;
    }
}