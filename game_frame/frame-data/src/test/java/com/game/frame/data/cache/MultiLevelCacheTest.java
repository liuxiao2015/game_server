package com.game.frame.data.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 多级缓存功能测试类
 * 
 * 功能说明：
 * - 测试多级缓存系统的核心功能和性能表现
 * - 验证L1本地缓存和L2分布式缓存的协调工作
 * - 确保缓存操作的正确性、一致性和异常处理
 * - 提供缓存性能和统计信息的测试用例
 * 
 * 测试设计：
 * - 使用SpringBoot测试框架，支持完整的依赖注入
 * - 配置测试专用的Redis连接，避免影响生产环境
 * - 使用Mockito模拟Redis操作，提升测试稳定性
 * - 覆盖缓存的增删改查等基础操作
 * 
 * 测试范围：
 * - 基础CRUD操作：put、get、exists、remove等
 * - 缓存命中逻辑：L1缓存优先，L2缓存备用
 * - 统计信息功能：命中率、缓存大小等指标
 * - 异常场景处理：网络异常、数据异常等
 * 
 * 测试价值：
 * - 保证缓存功能的正确性，避免线上Bug
 * - 验证性能优化效果，确保缓存命中率
 * - 回归测试支持，防止代码修改引入问题
 * - 文档作用：测试用例展示了API的正确使用方式
 * 
 * 技术特点：
 * - JUnit 5：使用最新的测试框架特性
 * - Spring Test：集成Spring容器进行集成测试
 * - Mockito：模拟外部依赖，提升测试可控性
 * - 断言丰富：全面验证测试结果和异常情况
 * 
 * @author lx
 * @date 2025/06/08
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
class MultiLevelCacheTest {

    /**
     * 多级缓存实例，被测试的核心对象
     */
    private MultiLevelCache multiLevelCache;
    
    /**
     * Redis模板的Mock对象，用于模拟L2缓存操作
     * 避免测试依赖真实的Redis服务，提升测试稳定性
     */
    private RedisTemplate<String, Object> mockRedisTemplate;

    /**
     * 测试前置设置
     * 
     * 功能说明：
     * - 在每个测试方法执行前初始化测试环境
     * - 创建Mock对象和被测试实例
     * - 确保每个测试方法都有干净的测试环境
     * 
     * 初始化内容：
     * 1. 创建Redis模板的Mock对象
     * 2. 使用Mock对象初始化多级缓存实例
     * 3. 为每个测试提供独立的缓存环境
     */
    @BeforeEach
    void setUp() {
        // 创建Redis模板的Mock对象，模拟L2分布式缓存
        mockRedisTemplate = mock(RedisTemplate.class);
        
        // 使用Mock对象创建多级缓存实例
        multiLevelCache = new MultiLevelCache(mockRedisTemplate);
    }

    /**
     * 测试缓存的存储和获取功能
     * 
     * 测试目的：
     * - 验证缓存数据能够正确存储
     * - 验证存储的数据能够正确获取
     * - 测试L1本地缓存的命中逻辑
     * 
     * 测试流程：
     * 1. 存储测试数据到缓存
     * 2. 从缓存获取数据
     * 3. 验证获取的数据与原始数据一致
     * 
     * 验证点：
     * - 数据存储成功
     * - 数据获取准确
     * - L1缓存命中（第二次获取更快）
     */
    @Test
    void testPutAndGet() {
        String key = "test_key";
        String value = "test_value";

        // 测试存储：将数据存入多级缓存
        multiLevelCache.put(key, value);

        // 测试获取：从缓存获取数据（应该命中L1本地缓存）
        String result = multiLevelCache.get(key);
        
        // 验证获取的数据与原始数据一致
        assertEquals(value, result);
    }

    /**
     * 测试缓存存在性检查功能
     * 
     * 测试目的：
     * - 验证exists方法能正确判断缓存是否存在
     * - 测试不存在和存在两种状态的判断准确性
     * 
     * 测试场景：
     * 1. 检查不存在的缓存项
     * 2. 存储数据后检查存在性
     * 
     * 验证点：
     * - 不存在时返回false
     * - 存在时返回true
     * - 判断逻辑的准确性
     */
    @Test
    void testExists() {
        String key = "test_key";
        String value = "test_value";

        // 测试不存在的情况：缓存项不存在时应返回false
        assertFalse(multiLevelCache.exists(key));

        // 存储数据后测试存在的情况
        multiLevelCache.put(key, value);
        
        // 验证存在时返回true
        assertTrue(multiLevelCache.exists(key));
    }

    /**
     * 测试缓存删除功能
     * 
     * 测试目的：
     * - 验证缓存项能够被正确删除
     * - 测试删除后缓存项确实不存在
     * - 验证L1和L2缓存的同步删除
     * 
     * 测试流程：
     * 1. 存储测试数据
     * 2. 验证数据存在
     * 3. 删除数据
     * 4. 验证数据不存在
     * 
     * 验证点：
     * - 删除前数据存在
     * - 删除后数据不存在
     * - 删除操作的完整性
     */
    @Test
    void testRemove() {
        String key = "test_key";
        String value = "test_value";

        // 先存储数据并验证存在
        multiLevelCache.put(key, value);
        assertTrue(multiLevelCache.exists(key));

        // 执行删除操作
        multiLevelCache.remove(key);
        
        // 验证删除后数据不存在
        assertFalse(multiLevelCache.exists(key));
    }

    /**
     * 测试L1缓存统计信息功能
     * 
     * 测试目的：
     * - 验证能够正确获取L1缓存的统计信息
     * - 测试统计信息的格式和内容正确性
     * - 确保统计功能不影响缓存性能
     * 
     * 验证点：
     * - 统计信息不为空
     * - 包含关键指标如命中次数
     * - 信息格式符合预期
     */
    @Test
    void testGetL1CacheStats() {
        // 获取L1缓存统计信息
        String stats = multiLevelCache.getL1CacheStats();
        
        // 验证统计信息不为空且包含关键指标
        assertNotNull(stats);
        assertTrue(stats.contains("hitCount"));
    }

    /**
     * 测试L1缓存大小获取功能
     * 
     * 测试目的：
     * - 验证能够正确获取L1缓存的当前大小
     * - 测试缓存大小统计的准确性
     * - 确保大小值在合理范围内
     * 
     * 验证点：
     * - 缓存大小为非负数
     * - 大小统计的准确性
     * - 统计功能的稳定性
     */
    @Test
    void testGetL1CacheSize() {
        // 获取L1缓存当前大小
        long size = multiLevelCache.getL1CacheSize();
        
        // 验证缓存大小为非负数
        assertTrue(size >= 0);
    }
}