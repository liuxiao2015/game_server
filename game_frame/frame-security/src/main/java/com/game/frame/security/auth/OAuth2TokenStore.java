package com.game.frame.security.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * OAuth Token存储
 * 实现Redis存储、过期管理
 * @author lx
 * @date 2025/06/08
 */
@Component
public class OAuth2TokenStore {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2TokenStore.class);
    
    private static final String OAUTH2_USER_PREFIX = "security:oauth2:user:";
    private static final String OAUTH2_SESSION_PREFIX = "security:oauth2:session:";
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 存储OAuth2用户信息
     */
    public void storeOAuth2UserInfo(String userId, String username) {
        try {
            String key = OAUTH2_USER_PREFIX + userId;
            redisTemplate.opsForValue().set(key, username, 30, TimeUnit.DAYS);
            
            logger.debug("OAuth2 user info stored for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to store OAuth2 user info for user: {}", userId, e);
        }
    }
    
    /**
     * 获取OAuth2用户信息
     */
    public String getOAuth2UserInfo(String userId) {
        try {
            String key = OAUTH2_USER_PREFIX + userId;
            String userInfo = redisTemplate.opsForValue().get(key);
            
            if (userInfo != null) {
                logger.debug("OAuth2 user info retrieved for user: {}", userId);
            } else {
                logger.debug("No OAuth2 user info found for user: {}", userId);
            }
            
            return userInfo;
        } catch (Exception e) {
            logger.error("Failed to retrieve OAuth2 user info for user: {}", userId, e);
            return null;
        }
    }
    
    /**
     * 存储OAuth2会话信息
     */
    public void storeOAuth2Session(String userId, String sessionData) {
        try {
            String key = OAUTH2_SESSION_PREFIX + userId;
            redisTemplate.opsForValue().set(key, sessionData, 24, TimeUnit.HOURS);
            
            logger.debug("OAuth2 session stored for user: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to store OAuth2 session for user: {}", userId, e);
        }
    }
    
    /**
     * 获取OAuth2会话信息
     */
    public String getOAuth2Session(String userId) {
        try {
            String key = OAUTH2_SESSION_PREFIX + userId;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Failed to get OAuth2 session for user: {}", userId, e);
            return null;
        }
    }
    
    /**
     * 撤销OAuth2信息
     */
    public void revokeOAuth2Info(String userId) {
        try {
            String userKey = OAUTH2_USER_PREFIX + userId;
            String sessionKey = OAUTH2_SESSION_PREFIX + userId;
            
            redisTemplate.delete(userKey);
            redisTemplate.delete(sessionKey);
            
            logger.debug("OAuth2 info revoked for user: {}", userId);
            
        } catch (Exception e) {
            logger.error("Failed to revoke OAuth2 info for user: {}", userId, e);
        }
    }
    
    /**
     * 检查OAuth2用户是否存在
     */
    public boolean hasOAuth2User(String userId) {
        try {
            String key = OAUTH2_USER_PREFIX + userId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Failed to check OAuth2 user existence for user: {}", userId, e);
            return false;
        }
    }
    
    /**
     * 清理过期的OAuth2数据
     */
    public void cleanupExpiredData() {
        try {
            // Redis会自动清理过期的键，这里主要用于日志记录
            logger.debug("OAuth2 data cleanup completed");
        } catch (Exception e) {
            logger.error("Failed to cleanup expired OAuth2 data", e);
        }
    }
}