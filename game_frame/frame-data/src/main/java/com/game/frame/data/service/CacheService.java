package com.game.frame.data.service;

import com.game.frame.data.cache.MultiLevelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

/**
 * 缓存服务
 * @author lx
 * @date 2025/06/08
 */
@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Autowired
    private MultiLevelCache multiLevelCache;

    /**
     * 获取缓存值，如果不存在则通过loader加载
     * @param key 缓存键
     * @param loader 加载器
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Callable<T> loader) {
        T value = multiLevelCache.get(key);
        if (value != null) {
            return value;
        }

        try {
            value = loader.call();
            if (value != null) {
                multiLevelCache.put(key, value);
            }
            return value;
        } catch (Exception e) {
            logger.error("Failed to load data for key: {}", key, e);
            return null;
        }
    }

    /**
     * 获取缓存值，如果不存在则通过loader加载（带过期时间）
     * @param key 缓存键
     * @param loader 加载器
     * @param expireSeconds 过期时间（秒）
     * @return 缓存值
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Callable<T> loader, long expireSeconds) {
        T value = multiLevelCache.get(key);
        if (value != null) {
            return value;
        }

        try {
            value = loader.call();
            if (value != null) {
                multiLevelCache.put(key, value, expireSeconds);
            }
            return value;
        } catch (Exception e) {
            logger.error("Failed to load data for key: {}", key, e);
            return null;
        }
    }

    /**
     * 设置缓存
     * @param key 缓存键
     * @param value 缓存值
     */
    public void put(String key, Object value) {
        multiLevelCache.put(key, value);
    }

    /**
     * 设置缓存（带过期时间）
     * @param key 缓存键
     * @param value 缓存值
     * @param expireSeconds 过期时间（秒）
     */
    public void put(String key, Object value, long expireSeconds) {
        multiLevelCache.put(key, value, expireSeconds);
    }

    /**
     * 获取缓存
     * @param key 缓存键
     * @return 缓存值
     */
    public <T> T get(String key) {
        return multiLevelCache.get(key);
    }

    /**
     * 删除缓存
     * @param key 缓存键
     */
    public void remove(String key) {
        multiLevelCache.remove(key);
    }

    /**
     * 检查缓存是否存在
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean exists(String key) {
        return multiLevelCache.exists(key);
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        multiLevelCache.clear();
    }

    /**
     * 获取缓存统计信息
     * @return 统计信息
     */
    public String getStats() {
        return multiLevelCache.getL1CacheStats();
    }

    /**
     * 获取缓存大小
     * @return 缓存大小
     */
    public long getSize() {
        return multiLevelCache.getL1CacheSize();
    }
}