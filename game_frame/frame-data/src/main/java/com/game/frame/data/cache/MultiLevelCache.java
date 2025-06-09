package com.game.frame.data.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 多级缓存实现
 * @author lx
 * @date 2025/06/08
 */
@Component
public class MultiLevelCache {

    private static final Logger logger = LoggerFactory.getLogger(MultiLevelCache.class);

    private final Cache<String, Object> l1Cache;
    private final RedisTemplate<String, Object> redisTemplate;

    // L1缓存配置
    private static final int L1_MAX_SIZE = 10000;
    private static final int L1_EXPIRE_MINUTES = 30;

    // L2缓存配置
    private static final int L2_EXPIRE_HOURS = 2;

    public MultiLevelCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.l1Cache = Caffeine.newBuilder()
                .maximumSize(L1_MAX_SIZE)
                .expireAfterWrite(L1_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> 
                        logger.debug("L1 cache removed: key={}, cause={}", key, cause))
                .build();
    }

    /**
     * 获取缓存值
     * @param key 缓存键
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        // 1. 先从L1缓存获取
        Object value = l1Cache.getIfPresent(key);
        if (value != null) {
            logger.debug("L1 cache hit: key={}", key);
            return (T) value;
        }

        // 2. 从L2缓存获取
        try {
            value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                logger.debug("L2 cache hit: key={}", key);
                // 回写到L1缓存
                l1Cache.put(key, value);
                return (T) value;
            }
        } catch (Exception e) {
            logger.warn("Failed to get from L2 cache: key={}", key, e);
        }

        logger.debug("Cache miss: key={}", key);
        return null;
    }

    /**
     * 设置缓存值
     * @param key 缓存键
     * @param value 缓存值
     */
    public void put(String key, Object value) {
        if (key == null || value == null) {
            return;
        }

        // 同时写入L1和L2缓存
        l1Cache.put(key, value);
        
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofHours(L2_EXPIRE_HOURS));
            logger.debug("Cache put: key={}", key);
        } catch (Exception e) {
            logger.warn("Failed to put to L2 cache: key={}", key, e);
        }
    }

    /**
     * 设置缓存值并指定过期时间
     * @param key 缓存键
     * @param value 缓存值
     * @param expireSeconds 过期时间（秒）
     */
    public void put(String key, Object value, long expireSeconds) {
        if (key == null || value == null) {
            return;
        }

        l1Cache.put(key, value);
        
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(expireSeconds));
            logger.debug("Cache put with expire: key={}, expireSeconds={}", key, expireSeconds);
        } catch (Exception e) {
            logger.warn("Failed to put to L2 cache with expire: key={}", key, e);
        }
    }

    /**
     * 删除缓存
     * @param key 缓存键
     */
    public void remove(String key) {
        if (key == null) {
            return;
        }

        l1Cache.invalidate(key);
        
        try {
            redisTemplate.delete(key);
            logger.debug("Cache removed: key={}", key);
        } catch (Exception e) {
            logger.warn("Failed to remove from L2 cache: key={}", key, e);
        }
    }

    /**
     * 检查缓存是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean exists(String key) {
        if (key == null) {
            return false;
        }

        // 先检查L1缓存
        if (l1Cache.getIfPresent(key) != null) {
            return true;
        }

        // 再检查L2缓存
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.warn("Failed to check existence in L2 cache: key={}", key, e);
            return false;
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        l1Cache.invalidateAll();
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            logger.info("All caches cleared");
        } catch (Exception e) {
            logger.warn("Failed to clear L2 cache", e);
        }
    }

    /**
     * 获取L1缓存统计信息
     * @return 统计信息
     */
    public String getL1CacheStats() {
        return l1Cache.stats().toString();
    }

    /**
     * 获取L1缓存大小
     * @return 缓存大小
     */
    public long getL1CacheSize() {
        return l1Cache.estimatedSize();
    }
}