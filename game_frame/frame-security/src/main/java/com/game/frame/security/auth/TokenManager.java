package com.game.frame.security.auth;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Token管理器
 * @author lx
 * @date 2025/06/08
 */
@Component
public class TokenManager {
    private static final Logger logger = LoggerFactory.getLogger(TokenManager.class);
    
    private static final String TOKEN_CACHE_PREFIX = "security:token:";
    private static final String BLACKLIST_PREFIX = "security:blacklist:";

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Token缓存（Redis）
     */
    public void cacheToken(String sessionId, String token, long expireSeconds) {
        try {
            String key = TOKEN_CACHE_PREFIX + sessionId;
            redisTemplate.opsForValue().set(key, token, expireSeconds, TimeUnit.SECONDS);
            logger.debug("Token cached for session: {}", sessionId);
        } catch (Exception e) {
            logger.error("Failed to cache token for session: {}", sessionId, e);
        }
    }

    /**
     * 从缓存获取Token
     */
    public String getCachedToken(String sessionId) {
        try {
            String key = TOKEN_CACHE_PREFIX + sessionId;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Failed to get cached token for session: {}", sessionId, e);
            return null;
        }
    }

    /**
     * 移除缓存的Token
     */
    public void removeCachedToken(String sessionId) {
        try {
            String key = TOKEN_CACHE_PREFIX + sessionId;
            redisTemplate.delete(key);
            logger.debug("Token removed from cache for session: {}", sessionId);
        } catch (Exception e) {
            logger.error("Failed to remove cached token for session: {}", sessionId, e);
        }
    }

    /**
     * 黑名单机制
     */
    public void addToBlacklist(String token, long expireSeconds) {
        try {
            // Use token hash to save space
            String tokenHash = String.valueOf(token.hashCode());
            String key = BLACKLIST_PREFIX + tokenHash;
            redisTemplate.opsForValue().set(key, "1", expireSeconds, TimeUnit.SECONDS);
            logger.debug("Token added to blacklist: {}", tokenHash);
        } catch (Exception e) {
            logger.error("Failed to add token to blacklist", e);
        }
    }

    /**
     * 检查Token是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        try {
            String tokenHash = String.valueOf(token.hashCode());
            String key = BLACKLIST_PREFIX + tokenHash;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Failed to check token blacklist", e);
            return false;
        }
    }

    /**
     * 多设备管理 - 踢出其他设备
     */
    public void kickOtherDevices(Long userId, String currentSessionId) {
        try {
            // This would require tracking all sessions per user
            // For now, we'll implement a simple version
            String pattern = TOKEN_CACHE_PREFIX + "*";
            // In a real implementation, you'd need to track user sessions
            logger.info("Kicking other devices for user: {}, current session: {}", userId, currentSessionId);
        } catch (Exception e) {
            logger.error("Failed to kick other devices for user: {}", userId, e);
        }
    }

    /**
     * Token续期策略
     */
    public String renewToken(String oldToken) {
        try {
            if (isBlacklisted(oldToken)) {
                logger.warn("Attempted to renew blacklisted token");
                return null;
            }

            String newToken = jwtTokenProvider.refreshToken(oldToken);
            if (newToken != null) {
                // Extract session ID from old token to update cache
                Claims claims = jwtTokenProvider.parseToken(oldToken);
                if (claims != null) {
                    String sessionId = claims.get("sessionId", String.class);
                    if (sessionId != null) {
                        // Update cached token
                        cacheToken(sessionId, newToken, 7200); // 2 hours default
                        
                        // Optionally blacklist old token
                        addToBlacklist(oldToken, 3600); // 1 hour grace period
                    }
                }
                logger.debug("Token renewed successfully");
                return newToken;
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Failed to renew token", e);
            return null;
        }
    }

    /**
     * 验证Token（综合检查）
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // Check blacklist first
        if (isBlacklisted(token)) {
            logger.warn("Token is blacklisted");
            return false;
        }

        // Validate JWT token
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 登出 - 清理所有相关的Token数据
     */
    public void logout(String token) {
        try {
            Claims claims = jwtTokenProvider.parseToken(token);
            if (claims != null) {
                String sessionId = claims.get("sessionId", String.class);
                if (sessionId != null) {
                    // Remove from cache
                    removeCachedToken(sessionId);
                }
                
                // Add to blacklist
                long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();
                if (remainingTime > 0) {
                    addToBlacklist(token, remainingTime / 1000);
                }
            }
            logger.debug("User logged out successfully");
        } catch (Exception e) {
            logger.error("Failed to logout user", e);
        }
    }
}