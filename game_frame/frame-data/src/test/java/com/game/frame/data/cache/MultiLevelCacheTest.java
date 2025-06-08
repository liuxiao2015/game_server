package com.game.frame.data.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 多级缓存测试
 * @author lx
 * @date 2025/06/08
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
class MultiLevelCacheTest {

    private MultiLevelCache multiLevelCache;
    private RedisTemplate<String, Object> mockRedisTemplate;

    @BeforeEach
    void setUp() {
        mockRedisTemplate = mock(RedisTemplate.class);
        multiLevelCache = new MultiLevelCache(mockRedisTemplate);
    }

    @Test
    void testPutAndGet() {
        String key = "test_key";
        String value = "test_value";

        // 测试存储
        multiLevelCache.put(key, value);

        // 测试获取（L1缓存命中）
        String result = multiLevelCache.get(key);
        assertEquals(value, result);
    }

    @Test
    void testExists() {
        String key = "test_key";
        String value = "test_value";

        // 不存在时
        assertFalse(multiLevelCache.exists(key));

        // 存在时
        multiLevelCache.put(key, value);
        assertTrue(multiLevelCache.exists(key));
    }

    @Test
    void testRemove() {
        String key = "test_key";
        String value = "test_value";

        // 先存储
        multiLevelCache.put(key, value);
        assertTrue(multiLevelCache.exists(key));

        // 删除
        multiLevelCache.remove(key);
        assertFalse(multiLevelCache.exists(key));
    }

    @Test
    void testGetL1CacheStats() {
        String stats = multiLevelCache.getL1CacheStats();
        assertNotNull(stats);
        assertTrue(stats.contains("hitCount"));
    }

    @Test
    void testGetL1CacheSize() {
        long size = multiLevelCache.getL1CacheSize();
        assertTrue(size >= 0);
    }
}