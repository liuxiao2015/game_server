package com.game.frame.data.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis缓存配置类
 * 
 * 功能说明：
 * - 配置Redis缓存服务的连接和序列化设置，支持游戏数据的高性能缓存
 * - 提供统一的RedisTemplate配置，简化Redis操作的开发复杂度
 * - 优化序列化方案，平衡存储效率和数据可读性
 * - 支持分布式缓存和会话管理，提升系统性能和用户体验
 * 
 * 设计思路：
 * - 采用String序列化器处理Key，确保键名的可读性和兼容性
 * - 使用JSON序列化器处理Value，支持复杂对象的序列化存储
 * - 分离Key和Value的序列化策略，灵活应对不同数据类型
 * - 集成Spring Redis，利用连接池和自动配置特性
 * 
 * 使用场景：
 * - 用户会话信息的缓存存储，支持分布式会话管理
 * - 游戏状态数据的临时缓存，提升数据访问性能
 * - 热点数据的快速检索，减少数据库访问压力
 * - 分布式锁和计数器的实现，支持并发控制
 * 
 * 技术特点：
 * - 高性能的内存存储，毫秒级数据访问
 * - 支持多种数据结构：String、Hash、List、Set、ZSet
 * - 自动过期机制，避免内存泄漏
 * - 主从复制和集群部署支持
 * 
 * 性能优化：
 * - 合理的序列化策略，减少网络传输开销
 * - 连接池复用，提升连接效率
 * - 批量操作支持，减少网络往返次数
 * 
 * @author lx
 * @date 2025/06/08
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate实例
     * 
     * 功能说明：
     * - 创建和配置RedisTemplate实例，提供统一的Redis操作接口
     * - 设置序列化器，优化数据存储格式和传输效率
     * - 支持String类型的Key和Object类型的Value操作
     * 
     * 业务逻辑：
     * 1. 创建RedisTemplate实例并设置连接工厂
     * 2. 配置Key序列化器为StringRedisSerializer，确保键名可读性
     * 3. 配置Value序列化器为GenericJackson2JsonRedisSerializer，支持对象序列化
     * 4. 分别设置Hash结构的Key和Value序列化器，保持一致性
     * 5. 调用afterPropertiesSet完成初始化设置
     * 
     * 序列化策略：
     * - Key使用String序列化：简单高效，便于调试和运维
     * - Value使用JSON序列化：支持复杂对象，保持数据结构完整性
     * - Hash结构采用相同策略，确保数据一致性
     * 
     * 性能考虑：
     * - JSON序列化虽然有一定开销，但提供了良好的可读性和跨语言兼容性
     * - String Key序列化开销最小，适合高频访问场景
     * - 连接工厂支持连接池，复用连接提升性能
     * 
     * @param connectionFactory Redis连接工厂，由Spring自动注入
     * @return 配置完成的RedisTemplate实例，支持String Key和Object Value操作
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂，支持连接池和集群模式
        template.setConnectionFactory(connectionFactory);

        // 配置Key序列化器 - 使用String序列化器确保键名的可读性和兼容性
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 配置Value序列化器 - 使用JSON序列化器支持复杂对象的存储和检索
        GenericJackson2JsonRedisSerializer jsonRedisSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        // 完成属性设置，触发初始化流程
        template.afterPropertiesSet();
        return template;
    }
}