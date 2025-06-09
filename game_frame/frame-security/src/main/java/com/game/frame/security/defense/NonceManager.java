package com.game.frame.security.defense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * Nonce管理器
 * 实现防重放攻击的唯一标识管理，使用滑动窗口和过期清理策略
 * @author lx
 * @date 2025/06/08
 */
@Component
public class NonceManager {
    
    private static final Logger logger = LoggerFactory.getLogger(NonceManager.class);
    
    private static final String NONCE_PREFIX = "security:nonce:";
    private static final String NONCE_WINDOW_PREFIX = "security:nonce:window:";
    
    // 默认滑动窗口大小（秒）
    private static final int DEFAULT_WINDOW_SIZE = 300; // 5分钟
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 生成唯一的Nonce
     */
    public String generateNonce() {
        return generateNonce(16);
    }
    
    /**
     * 生成指定长度的Nonce
     */
    public String generateNonce(int length) {
        try {
            byte[] bytes = new byte[length];
            secureRandom.nextBytes(bytes);
            
            StringBuilder nonce = new StringBuilder();
            for (byte b : bytes) {
                nonce.append(String.format("%02x", b & 0xff));
            }
            
            String nonceStr = nonce.toString();
            logger.debug("Generated nonce: {}", nonceStr);
            return nonceStr;
            
        } catch (Exception e) {
            logger.error("Failed to generate nonce", e);
            throw new RuntimeException("Failed to generate nonce", e);
        }
    }
    
    /**
     * 验证Nonce是否有效（未被使用过且在时间窗口内）
     */
    public boolean validateNonce(String nonce, long timestamp) {
        return validateNonce(nonce, timestamp, DEFAULT_WINDOW_SIZE);
    }
    
    /**
     * 验证Nonce是否有效
     */
    public boolean validateNonce(String nonce, long timestamp, int windowSizeSeconds) {
        try {
            if (nonce == null || nonce.trim().isEmpty()) {
                logger.warn("Empty nonce provided");
                return false;
            }
            
            // 检查时间窗口
            long currentTime = System.currentTimeMillis() / 1000;
            long timeDiff = Math.abs(currentTime - timestamp);
            
            if (timeDiff > windowSizeSeconds) {
                logger.warn("Nonce timestamp outside valid window: {} seconds difference", timeDiff);
                return false;
            }
            
            // 检查Nonce是否已被使用
            String key = NONCE_PREFIX + nonce;
            Boolean exists = redisTemplate.hasKey(key);
            
            if (Boolean.TRUE.equals(exists)) {
                logger.warn("Nonce already used: {}", nonce);
                return false;
            }
            
            // 标记Nonce为已使用
            markNonceAsUsed(nonce, windowSizeSeconds);
            
            logger.debug("Nonce validated successfully: {}", nonce);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to validate nonce: {}", nonce, e);
            return false;
        }
    }
    
    /**
     * 标记Nonce为已使用
     */
    public void markNonceAsUsed(String nonce, int ttlSeconds) {
        try {
            String key = NONCE_PREFIX + nonce;
            String value = String.valueOf(System.currentTimeMillis());
            
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
            
            logger.debug("Nonce marked as used: {}", nonce);
            
        } catch (Exception e) {
            logger.error("Failed to mark nonce as used: {}", nonce, e);
        }
    }
    
    /**
     * 检查Nonce是否已被使用
     */
    public boolean isNonceUsed(String nonce) {
        try {
            String key = NONCE_PREFIX + nonce;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Failed to check if nonce is used: {}", nonce, e);
            return false;
        }
    }
    
    /**
     * 批量验证Nonce（用于处理批量请求）
     */
    public boolean validateNonceBatch(String[] nonces, long timestamp, int windowSizeSeconds) {
        try {
            if (nonces == null || nonces.length == 0) {
                return false;
            }
            
            // 检查时间窗口
            long currentTime = System.currentTimeMillis() / 1000;
            long timeDiff = Math.abs(currentTime - timestamp);
            
            if (timeDiff > windowSizeSeconds) {
                logger.warn("Batch nonce timestamp outside valid window: {} seconds difference", timeDiff);
                return false;
            }
            
            // 批量检查Nonce是否已被使用
            for (String nonce : nonces) {
                if (isNonceUsed(nonce)) {
                    logger.warn("Batch validation failed: nonce already used: {}", nonce);
                    return false;
                }
            }
            
            // 批量标记Nonce为已使用
            for (String nonce : nonces) {
                markNonceAsUsed(nonce, windowSizeSeconds);
            }
            
            logger.debug("Batch nonce validation successful: {} nonces", nonces.length);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to validate batch nonces", e);
            return false;
        }
    }
    
    /**
     * 清理过期的Nonce
     */
    public void cleanupExpiredNonces() {
        try {
            // Redis会自动清理过期的键，这里主要用于统计和日志
            logger.debug("Nonce cleanup completed");
        } catch (Exception e) {
            logger.error("Failed to cleanup expired nonces", e);
        }
    }
    
    /**
     * 获取时间窗口内的Nonce统计信息
     */
    public NonceStatistics getNonceStatistics(int windowSizeSeconds) {
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            String windowKey = NONCE_WINDOW_PREFIX + (currentTime / windowSizeSeconds);
            
            // 获取当前窗口的统计信息
            String stats = redisTemplate.opsForValue().get(windowKey);
            
            if (stats != null) {
                String[] parts = stats.split(":");
                if (parts.length == 2) {
                    return new NonceStatistics(
                        Integer.parseInt(parts[0]), // total requests
                        Integer.parseInt(parts[1])  // unique nonces
                    );
                }
            }
            
            return new NonceStatistics(0, 0);
            
        } catch (Exception e) {
            logger.error("Failed to get nonce statistics", e);
            return new NonceStatistics(0, 0);
        }
    }
    
    /**
     * 更新Nonce统计信息
     */
    public void updateNonceStatistics(int windowSizeSeconds) {
        try {
            long currentTime = System.currentTimeMillis() / 1000;
            String windowKey = NONCE_WINDOW_PREFIX + (currentTime / windowSizeSeconds);
            
            // 简化更新统计的实现
            String script = "local key = KEYS[1]; local ttl = ARGV[1]; " +
                           "local current = redis.call('GET', key); " +
                           "if current then " +
                           "  local count = tonumber(current) + 1; " +
                           "  redis.call('SET', key, count, 'EX', ttl); " +
                           "else " +
                           "  redis.call('SET', key, '1', 'EX', ttl); " +
                           "end; " +
                           "return redis.call('GET', key)";
            
            redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                return connection.eval(script.getBytes(), 
                    org.springframework.data.redis.connection.ReturnType.VALUE, 
                    1, 
                    windowKey.getBytes(), 
                    String.valueOf(windowSizeSeconds).getBytes());
            });
            
        } catch (Exception e) {
            logger.error("Failed to update nonce statistics", e);
        }
    }
    
    /**
     * 检查请求频率是否异常
     */
    public boolean checkRequestFrequency(String clientId, int maxRequestsPerMinute) {
        try {
            long currentMinute = System.currentTimeMillis() / 60000; // 当前分钟
            String frequencyKey = "security:frequency:" + clientId + ":" + currentMinute;
            
            Long currentCount = redisTemplate.opsForValue().increment(frequencyKey);
            if (currentCount == 1) {
                // 设置过期时间
                redisTemplate.expire(frequencyKey, 60, TimeUnit.SECONDS);
            }
            
            if (currentCount > maxRequestsPerMinute) {
                logger.warn("Request frequency exceeded for client: {} ({} requests)", clientId, currentCount);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to check request frequency for client: {}", clientId, e);
            return true; // 默认允许，避免误拦截
        }
    }
    
    /**
     * Nonce统计信息
     */
    public static class NonceStatistics {
        private final int totalRequests;
        private final int uniqueNonces;
        
        public NonceStatistics(int totalRequests, int uniqueNonces) {
            this.totalRequests = totalRequests;
            this.uniqueNonces = uniqueNonces;
        }
        
        public int getTotalRequests() { return totalRequests; }
        public int getUniqueNonces() { return uniqueNonces; }
        
        public double getDuplicateRate() {
            if (totalRequests == 0) return 0.0;
            return (double) (totalRequests - uniqueNonces) / totalRequests;
        }
        
        @Override
        public String toString() {
            return String.format("NonceStatistics{totalRequests=%d, uniqueNonces=%d, duplicateRate=%.2f%%}", 
                totalRequests, uniqueNonces, getDuplicateRate() * 100);
        }
    }
}