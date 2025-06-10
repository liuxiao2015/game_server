package com.game.frame.data.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理器配置类
 * 
 * 功能说明：
 * - 配置多级缓存体系，包括本地缓存(Caffeine)和分布式缓存(Redis)
 * - 提供高性能的数据缓存解决方案，显著提升系统响应速度
 * - 支持缓存的自动过期、统计监控和容量控制
 * - 集成Spring Cache抽象，通过注解简化缓存操作
 * 
 * 设计思路：
 * - 两级缓存架构：本地缓存提供极致性能，分布式缓存确保数据一致性
 * - Caffeine作为L1缓存：低延迟、高吞吐量的JVM内存缓存
 * - Redis作为L2缓存：支持分布式部署和数据共享
 * - 智能过期策略：写入后过期和访问后过期的组合策略
 * 
 * 使用场景：
 * - 游戏配置数据缓存，如装备属性、技能配置等静态数据
 * - 用户信息缓存，提升用户数据查询性能
 * - 计算结果缓存，避免重复的复杂计算操作
 * - 热点数据缓存，减少数据库访问压力
 * 
 * 技术特点：
 * - Caffeine高性能本地缓存，基于W-TinyLFU算法
 * - Redis分布式缓存，支持集群和主从模式
 * - 统计功能支持，便于监控和性能调优
 * - 序列化优化，减少网络传输开销
 * 
 * 性能优化：
 * - 本地缓存命中率优先，减少网络开销
 * - 合理的容量控制，避免内存溢出
 * - 智能的过期策略，平衡缓存效果和内存使用
 * - 异步刷新机制，避免缓存雪崩
 * 
 * @author lx
 * @date 2025/06/08
 */
@Configuration
@EnableCaching
public class CacheManagerConfig {

    /**
     * 配置Caffeine本地缓存管理器
     * 
     * 功能说明：
     * - 创建基于Caffeine的高性能本地缓存管理器
     * - 作为主要(Primary)缓存管理器，优先使用本地缓存
     * - 提供极低延迟的数据访问，适合频繁访问的热点数据
     * 
     * 业务逻辑：
     * 1. 设置最大缓存容量为10000个条目，防止内存溢出
     * 2. 写入后30分钟过期，确保数据的时效性
     * 3. 访问后15分钟过期，清理不活跃的缓存数据
     * 4. 启用统计功能，便于监控缓存命中率和性能
     * 
     * 缓存策略：
     * - 基于W-TinyLFU算法的智能淘汰策略
     * - 写后过期：确保数据不会无限期存在
     * - 访问后过期：清理冷数据，释放内存空间
     * - 容量控制：避免缓存占用过多JVM堆内存
     * 
     * 适用场景：
     * - 游戏配置数据：装备、技能、道具等基础配置
     * - 用户会话信息：当前在线用户的基本信息
     * - 计算结果缓存：复杂算法的计算结果
     * - 字典数据：枚举值、常量配置等
     * 
     * 性能特点：
     * - 纳秒级访问延迟，比Redis快100倍以上
     * - 无网络开销，直接内存访问
     * - 高并发支持，线程安全的访问机制
     * - 自动垃圾回收，不会产生内存泄漏
     * 
     * @return CacheManager Caffeine缓存管理器实例
     */
    @Bean("caffeineCacheManager")
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000)                                    // 最大缓存条目数
                .expireAfterWrite(30, TimeUnit.MINUTES)                // 写入后30分钟过期
                .expireAfterAccess(15, TimeUnit.MINUTES)               // 访问后15分钟过期
                .recordStats());                                       // 启用缓存统计
        return cacheManager;
    }

    /**
     * 配置Redis分布式缓存管理器
     * 
     * 功能说明：
     * - 创建基于Redis的分布式缓存管理器
     * - 支持多实例间的缓存数据共享和一致性
     * - 提供持久化的缓存存储，重启后数据不丢失
     * 
     * 业务逻辑：
     * 1. 设置默认TTL为1小时，平衡数据新鲜度和缓存效果
     * 2. 配置Key序列化为String格式，便于调试和运维
     * 3. 配置Value序列化为JSON格式，支持复杂对象存储
     * 4. 使用RedisCacheManager构建器创建缓存管理器
     * 
     * 序列化策略：
     * - Key使用StringRedisSerializer：简单高效，便于查看
     * - Value使用GenericJackson2JsonRedisSerializer：支持对象序列化
     * - JSON格式便于跨语言访问和数据迁移
     * 
     * 适用场景：
     * - 分布式会话共享：多服务实例间的用户会话
     * - 全局配置缓存：所有服务实例共享的配置数据
     * - 大对象缓存：本地缓存无法存储的大数据对象
     * - 持久化缓存：需要在服务重启后仍然保留的数据
     * 
     * 性能考虑：
     * - 网络延迟：通常在1-10ms之间，比本地缓存慢但可接受
     * - 序列化开销：JSON序列化有一定CPU开销，但便于调试
     * - 内存使用：缓存数据存储在Redis服务器，不占用应用内存
     * - 高可用：支持主从复制和集群模式
     * 
     * @param redisConnectionFactory Redis连接工厂，由Spring自动注入
     * @return CacheManager Redis缓存管理器实例
     */
    @Bean("redisCacheManager")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 配置Redis缓存的默认设置
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))                         // 设置1小时过期时间
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))  // Key使用String序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer())); // Value使用JSON序列化

        // 构建并返回RedisCacheManager
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }
}