package com.game.frame.security.defense;

import com.game.frame.security.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 限流服务
 * @author lx
 * @date 2025/06/08
 */
@Service
public class RateLimitService {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);
    
    private static final String RATE_LIMIT_PREFIX = "security:ratelimit:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SecurityProperties securityProperties;

    // Lua script for atomic rate limiting with sliding window
    private static final String RATE_LIMIT_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local window = tonumber(ARGV[1])\n" +
        "local limit = tonumber(ARGV[2])\n" +
        "local current_time = tonumber(ARGV[3])\n" +
        "\n" +
        "-- Remove expired entries\n" +
        "redis.call('ZREMRANGEBYSCORE', key, 0, current_time - window * 1000)\n" +
        "\n" +
        "-- Count current requests\n" +
        "local current_requests = redis.call('ZCARD', key)\n" +
        "\n" +
        "if current_requests < limit then\n" +
        "    -- Add current request\n" +
        "    redis.call('ZADD', key, current_time, current_time)\n" +
        "    redis.call('EXPIRE', key, window)\n" +
        "    return {1, limit - current_requests - 1}\n" +
        "else\n" +
        "    return {0, 0}\n" +
        "end";

    private final DefaultRedisScript<Object> rateLimitScript;

    public RateLimitService() {
        rateLimitScript = new DefaultRedisScript<>();
        rateLimitScript.setScriptText(RATE_LIMIT_SCRIPT);
        rateLimitScript.setResultType(Object.class);
    }

    /**
     * IP限流
     */
    public boolean isAllowedByIp(String ip) {
        return isAllowed(RATE_LIMIT_PREFIX + "ip:" + ip, 
                        securityProperties.getRateLimit().getDefaultQps(), 
                        60);
    }

    /**
     * 用户限流
     */
    public boolean isAllowedByUser(Long userId) {
        return isAllowed(RATE_LIMIT_PREFIX + "user:" + userId, 
                        securityProperties.getRateLimit().getDefaultQps(), 
                        60);
    }

    /**
     * API限流
     */
    public boolean isAllowedByApi(String apiPath) {
        return isAllowed(RATE_LIMIT_PREFIX + "api:" + apiPath, 
                        securityProperties.getRateLimit().getDefaultQps(), 
                        60);
    }

    /**
     * 自定义限流
     */
    public boolean isAllowed(String key, int limit, int windowSeconds) {
        try {
            Object result = redisTemplate.execute(rateLimitScript, 
                    Collections.singletonList(key),
                    String.valueOf(windowSeconds),
                    String.valueOf(limit),
                    String.valueOf(System.currentTimeMillis()));

            if (result instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Object> list = (java.util.List<Object>) result;
                if (list.size() >= 1) {
                    Object allowedObj = list.get(0);
                    if (allowedObj instanceof Number) {
                        boolean allowed = ((Number) allowedObj).intValue() == 1;
                        if (!allowed) {
                            logger.warn("Rate limit exceeded for key: {}", key);
                        }
                        return allowed;
                    }
                }
            }
            
            // Default to allowing if script execution fails
            logger.warn("Unexpected rate limit script result: {}", result);
            return true;
            
        } catch (Exception e) {
            logger.error("Rate limit check failed for key: {}", key, e);
            // Default to allowing on error to maintain service availability
            return true;
        }
    }

    /**
     * 获取剩余请求次数
     */
    public long getRemainingRequests(String key, int limit, int windowSeconds) {
        try {
            String fullKey = RATE_LIMIT_PREFIX + key;
            long currentTime = System.currentTimeMillis();
            
            // Remove expired entries
            redisTemplate.opsForZSet().removeRangeByScore(fullKey, 0, currentTime - windowSeconds * 1000L);
            
            // Count current requests
            Long currentRequests = redisTemplate.opsForZSet().count(fullKey, 
                    currentTime - windowSeconds * 1000L, currentTime);
            
            return Math.max(0, limit - (currentRequests != null ? currentRequests : 0));
            
        } catch (Exception e) {
            logger.error("Failed to get remaining requests for key: {}", key, e);
            return 0;
        }
    }

    /**
     * 清空指定key的限流记录
     */
    public void resetRateLimit(String key) {
        try {
            redisTemplate.delete(RATE_LIMIT_PREFIX + key);
            logger.info("Rate limit reset for key: {}", key);
        } catch (Exception e) {
            logger.error("Failed to reset rate limit for key: {}", key, e);
        }
    }

    /**
     * 基于令牌桶算法的限流
     */
    public boolean isAllowedTokenBucket(String key, int capacity, int refillRate) {
        try {
            String bucketKey = RATE_LIMIT_PREFIX + "bucket:" + key;
            String tokensKey = bucketKey + ":tokens";
            String lastRefillKey = bucketKey + ":lastRefill";
            
            long currentTime = System.currentTimeMillis();
            
            // Get current tokens and last refill time
            String tokensStr = redisTemplate.opsForValue().get(tokensKey);
            String lastRefillStr = redisTemplate.opsForValue().get(lastRefillKey);
            
            int tokens = tokensStr != null ? Integer.parseInt(tokensStr) : capacity;
            long lastRefill = lastRefillStr != null ? Long.parseLong(lastRefillStr) : currentTime;
            
            // Calculate tokens to add based on time elapsed
            long timeDiff = currentTime - lastRefill;
            int tokensToAdd = (int) (timeDiff * refillRate / 1000); // refillRate per second
            
            tokens = Math.min(capacity, tokens + tokensToAdd);
            
            if (tokens > 0) {
                // Consume a token
                tokens--;
                
                // Update Redis
                redisTemplate.opsForValue().set(tokensKey, String.valueOf(tokens), 1, TimeUnit.HOURS);
                redisTemplate.opsForValue().set(lastRefillKey, String.valueOf(currentTime), 1, TimeUnit.HOURS);
                
                return true;
            } else {
                logger.warn("Token bucket empty for key: {}", key);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Token bucket rate limit check failed for key: {}", key, e);
            return true; // Default to allowing on error
        }
    }
}