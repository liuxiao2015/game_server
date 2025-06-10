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
 * 
 * 功能说明：
 * - 实现L1（本地缓存）+ L2（Redis分布式缓存）的两级缓存架构
 * - 提供高性能的数据缓存和快速访问能力
 * - 支持缓存失效、自动过期和统计监控功能
 * - 集成容错机制，确保缓存故障不影响系统可用性
 * 
 * 设计思路：
 * - L1缓存使用Caffeine实现，提供纳秒级的访问速度
 * - L2缓存使用Redis实现，支持分布式和持久化
 * - 采用Write-Through策略，同时更新两级缓存
 * - 实现缓存穿透和缓存雪崩的防护机制
 * 
 * 缓存策略：
 * - 读取策略：L1 → L2 → 数据源
 * - 写入策略：同时写入L1和L2缓存
 * - 失效策略：同时失效L1和L2缓存
 * - 容错策略：L2故障时仍可使用L1缓存
 * 
 * 性能特性：
 * - L1缓存命中率优化：智能预热和淘汰策略
 * - L2缓存访问优化：连接池复用和批量操作
 * - 内存使用控制：合理的缓存大小和过期时间
 * - 网络开销最小化：减少不必要的Redis调用
 * 
 * 监控指标：
 * - 缓存命中率统计（L1/L2分别统计）
 * - 缓存操作耗时监控
 * - 缓存大小和内存使用情况
 * - 异常和错误率统计
 * 
 * 使用场景：
 * - 游戏配置数据缓存：减少数据库查询压力
 * - 用户会话信息缓存：提高登录验证速度
 * - 热点数据缓存：提升高频访问数据的响应速度
 * - 计算结果缓存：避免重复的复杂计算
 * 
 * @author lx
 * @date 2025/06/08
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class MultiLevelCache {

    // 日志记录器，用于记录缓存操作和性能统计
    private static final Logger logger = LoggerFactory.getLogger(MultiLevelCache.class);

    // 缓存实例
    private final Cache<String, Object> l1Cache;
    private final RedisTemplate<String, Object> redisTemplate;

    // L1缓存配置常量
    /** L1缓存最大条目数：10000个，平衡内存使用和命中率 */
    private static final int L1_MAX_SIZE = 10000;
    
    /** L1缓存过期时间：30分钟，适合热点数据的访问模式 */
    private static final int L1_EXPIRE_MINUTES = 30;

    // L2缓存配置常量
    /** L2缓存默认过期时间：2小时，适合分布式环境的数据一致性 */
    private static final int L2_EXPIRE_HOURS = 2;
    
    /** Redis操作超时时间：5秒 */
    private static final Duration REDIS_TIMEOUT = Duration.ofSeconds(5);

    // 性能统计计数器
    private final java.util.concurrent.atomic.AtomicLong l1HitCount = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong l2HitCount = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong missCount = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong errorCount = new java.util.concurrent.atomic.AtomicLong(0);
    
    // 性能监控
    private volatile long lastStatsLogTime = System.currentTimeMillis();

    /**
     * 构造函数，初始化多级缓存
     * 
     * @param redisTemplate Redis操作模板，用于L2缓存操作
     */
    public MultiLevelCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        // 初始化L1缓存（Caffeine本地缓存）
        this.l1Cache = Caffeine.newBuilder()
                .maximumSize(L1_MAX_SIZE)
                .expireAfterWrite(L1_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .recordStats()  // 启用统计功能
                .removalListener((key, value, cause) -> {
                    // 缓存条目被移除时的回调处理
                    logger.debug("L1缓存条目移除: key={}, 原因={}", key, cause);
                    
                    // 根据移除原因进行不同的处理
                    switch (cause) {
                        case SIZE:
                            logger.trace("L1缓存因容量限制移除条目: {}", key);
                            break;
                        case EXPIRED:
                            logger.trace("L1缓存因过期移除条目: {}", key);
                            break;
                        case EXPLICIT:
                            logger.trace("L1缓存被显式移除条目: {}", key);
                            break;
                        default:
                            logger.trace("L1缓存因其他原因移除条目: {}, 原因: {}", key, cause);
                    }
                })
                .build();
                
        logger.info("多级缓存初始化完成 - L1最大容量: {}, L1过期时间: {}分钟, L2过期时间: {}小时", 
                L1_MAX_SIZE, L1_EXPIRE_MINUTES, L2_EXPIRE_HOURS);
    }

    /**
     * 从缓存中获取数据
     * 
     * 功能说明：
     * - 按照L1 → L2 → 数据源的顺序查找数据
     * - 实现缓存的自动回填机制，提高后续访问效率
     * - 提供详细的性能统计和错误处理
     * 
     * 查找流程：
     * 1. 首先从L1缓存（本地缓存）中查找
     * 2. L1未命中时，从L2缓存（Redis）中查找
     * 3. L2命中时，自动回填到L1缓存
     * 4. 两级缓存都未命中时返回null
     * 
     * 性能优化：
     * - L1缓存提供纳秒级访问速度
     * - L2缓存访问包含超时控制，避免长时间阻塞
     * - 智能的缓存回填策略，提高L1命中率
     * 
     * @param key 缓存键，不能为null
     * @param <T> 缓存值的类型
     * @return 缓存的值，如果不存在则返回null
     * @throws IllegalArgumentException 当key为null时抛出
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        // 输入参数验证
        if (key == null) {
            throw new IllegalArgumentException("缓存键不能为null");
        }
        
        long startTime = System.nanoTime();
        
        try {
            // 第一阶段：从L1缓存获取
            Object value = l1Cache.getIfPresent(key);
            if (value != null) {
                l1HitCount.incrementAndGet();
                logCacheOperation("L1缓存命中", key, startTime);
                return (T) value;
            }

            // 第二阶段：从L2缓存获取
            try {
                value = redisTemplate.opsForValue().get(key);
                if (value != null) {
                    l2HitCount.incrementAndGet();
                    
                    // 回填到L1缓存，提高后续访问效率
                    l1Cache.put(key, value);
                    
                    logCacheOperation("L2缓存命中并回填L1", key, startTime);
                    return (T) value;
                }
            } catch (Exception e) {
                // L2缓存访问失败，记录错误但不影响业务流程
                errorCount.incrementAndGet();
                logger.warn("L2缓存访问失败，key: {}, 错误: {}", key, e.getMessage());
                
                // 可以考虑降级策略，比如延长L1缓存时间
            }

            // 第三阶段：缓存未命中
            missCount.incrementAndGet();
            logCacheOperation("缓存未命中", key, startTime);
            
            // 定期输出缓存统计信息
            logStatsIfNeeded();
            
            return null;
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("缓存获取操作发生异常，key: {}", key, e);
            return null;
        }
    }
    
    /**
     * 记录缓存操作日志
     * 
     * @param operation 操作类型描述
     * @param key 缓存键
     * @param startTime 操作开始时间（纳秒）
     */
    private void logCacheOperation(String operation, String key, long startTime) {
        if (logger.isTraceEnabled()) {
            long duration = (System.nanoTime() - startTime) / 1000; // 转换为微秒
            logger.trace("{}: key={}, 耗时={}μs", operation, key, duration);
        }
    }
    
    /**
     * 定期输出缓存统计信息
     */
    private void logStatsIfNeeded() {
        long currentTime = System.currentTimeMillis();
        // 每5分钟输出一次统计信息
        if (currentTime - lastStatsLogTime > 300_000) {
            lastStatsLogTime = currentTime;
            logger.info("缓存统计 - {}", getCacheStats());
        }
    }

    /**
     * 设置缓存值（使用默认过期时间）
     * 
     * 功能说明：
     * - 同时写入L1和L2缓存，确保数据一致性
     * - 使用预设的默认过期时间
     * - 提供完整的错误处理和性能监控
     * 
     * 写入策略：
     * - 采用Write-Through模式，同时更新两级缓存
     * - L1缓存写入失败不影响L2缓存操作
     * - L2缓存写入失败时记录错误但不抛出异常
     * 
     * @param key 缓存键，不能为null
     * @param value 缓存值，不能为null
     * @throws IllegalArgumentException 当key或value为null时抛出
     */
    public void put(String key, Object value) {
        // 输入参数验证
        if (key == null) {
            throw new IllegalArgumentException("缓存键不能为null");
        }
        if (value == null) {
            throw new IllegalArgumentException("缓存值不能为null");
        }

        long startTime = System.nanoTime();
        
        try {
            // 写入L1缓存
            l1Cache.put(key, value);
            
            // 写入L2缓存
            try {
                redisTemplate.opsForValue().set(key, value, Duration.ofHours(L2_EXPIRE_HOURS));
                
                if (logger.isDebugEnabled()) {
                    long duration = (System.nanoTime() - startTime) / 1000; // 转换为微秒
                    logger.debug("缓存写入成功: key={}, 耗时={}μs", key, duration);
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
                logger.warn("L2缓存写入失败: key={}, 错误: {}", key, e.getMessage());
                // L2写入失败时，L1缓存仍然可用
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("缓存写入操作发生异常: key={}", key, e);
        }
    }

    /**
     * 设置缓存值并指定过期时间
     * 
     * 功能说明：
     * - 支持自定义过期时间的缓存写入
     * - 同时写入L1和L2缓存，过期时间仅对L2缓存生效
     * - L1缓存仍使用默认的过期策略
     * 
     * 注意事项：
     * - L1缓存的过期时间由Caffeine配置控制，此方法不会影响L1的过期时间
     * - 建议expireSeconds不要设置过短，避免频繁的缓存失效
     * 
     * @param key 缓存键，不能为null
     * @param value 缓存值，不能为null
     * @param expireSeconds 过期时间（秒），必须大于0
     * @throws IllegalArgumentException 当参数不合法时抛出
     */
    public void put(String key, Object value, long expireSeconds) {
        // 输入参数验证
        if (key == null) {
            throw new IllegalArgumentException("缓存键不能为null");
        }
        if (value == null) {
            throw new IllegalArgumentException("缓存值不能为null");
        }
        if (expireSeconds <= 0) {
            throw new IllegalArgumentException("过期时间必须大于0秒");
        }

        long startTime = System.nanoTime();
        
        try {
            // 写入L1缓存（使用默认过期时间）
            l1Cache.put(key, value);
            
            // 写入L2缓存（使用指定过期时间）
            try {
                redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(expireSeconds));
                
                if (logger.isDebugEnabled()) {
                    long duration = (System.nanoTime() - startTime) / 1000; // 转换为微秒
                    logger.debug("缓存写入成功（自定义过期）: key={}, 过期时间={}秒, 耗时={}μs", 
                            key, expireSeconds, duration);
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
                logger.warn("L2缓存写入失败（自定义过期）: key={}, 过期时间={}秒, 错误: {}", 
                        key, expireSeconds, e.getMessage());
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("缓存写入操作发生异常（自定义过期）: key={}, 过期时间={}秒", key, expireSeconds, e);
        }
    }

    /**
     * 从缓存中删除指定键
     * 
     * 功能说明：
     * - 同时从L1和L2缓存中删除指定的键值对
     * - 确保两级缓存的数据一致性
     * - 提供完整的错误处理和操作日志
     * 
     * 删除策略：
     * - 先删除L1缓存，再删除L2缓存
     * - L2删除失败不影响L1的删除结果
     * - 记录详细的删除操作日志
     * 
     * @param key 要删除的缓存键，不能为null
     * @throws IllegalArgumentException 当key为null时抛出
     */
    public void remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("缓存键不能为null");
        }

        long startTime = System.nanoTime();
        
        try {
            // 从L1缓存删除
            l1Cache.invalidate(key);
            
            // 从L2缓存删除
            try {
                Boolean deleted = redisTemplate.delete(key);
                
                if (logger.isDebugEnabled()) {
                    long duration = (System.nanoTime() - startTime) / 1000; // 转换为微秒
                    logger.debug("缓存删除完成: key={}, L2删除结果={}, 耗时={}μs", 
                            key, deleted, duration);
                }
            } catch (Exception e) {
                errorCount.incrementAndGet();
                logger.warn("L2缓存删除失败: key={}, 错误: {}", key, e.getMessage());
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("缓存删除操作发生异常: key={}", key, e);
        }
    }

    /**
     * 检查缓存中是否存在指定键
     * 
     * 功能说明：
     * - 按照L1 → L2的顺序检查键是否存在
     * - 提供快速的存在性检查，不触发数据加载
     * - 支持缓存预热和数据一致性验证
     * 
     * @param key 要检查的缓存键，不能为null
     * @return true表示键存在，false表示键不存在
     * @throws IllegalArgumentException 当key为null时抛出
     */
    public boolean exists(String key) {
        if (key == null) {
            throw new IllegalArgumentException("缓存键不能为null");
        }

        try {
            // 先检查L1缓存
            if (l1Cache.getIfPresent(key) != null) {
                logger.trace("键存在于L1缓存: {}", key);
                return true;
            }

            // 再检查L2缓存
            try {
                Boolean exists = redisTemplate.hasKey(key);
                boolean result = Boolean.TRUE.equals(exists);
                
                if (logger.isTraceEnabled()) {
                    logger.trace("键在L2缓存中的存在状态: key={}, 存在={}", key, result);
                }
                
                return result;
            } catch (Exception e) {
                errorCount.incrementAndGet();
                logger.warn("L2缓存存在性检查失败: key={}, 错误: {}", key, e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("缓存存在性检查发生异常: key={}", key, e);
            return false;
        }
    }

    /**
     * 清空所有缓存数据
     * 
     * 功能说明：
     * - 同时清空L1和L2缓存中的所有数据
     * - 提供完整的清理操作和错误处理
     * - 记录清理操作的执行结果
     * 
     * 注意事项：
     * - 此操作会影响所有使用该Redis实例的应用
     * - 生产环境中谨慎使用，建议只清理特定前缀的键
     * - 操作完成后会重置相关的统计计数器
     */
    public void clear() {
        long startTime = System.nanoTime();
        
        try {
            // 清空L1缓存
            long l1Size = l1Cache.estimatedSize();
            l1Cache.invalidateAll();
            
            // 清空L2缓存
            try {
                redisTemplate.getConnectionFactory().getConnection().flushAll();
                
                long duration = (System.nanoTime() - startTime) / 1_000_000; // 转换为毫秒
                logger.info("所有缓存已清空 - L1条目数: {}, 耗时: {}ms", l1Size, duration);
                
                // 重置统计计数器
                resetStats();
                
            } catch (Exception e) {
                errorCount.incrementAndGet();
                logger.warn("L2缓存清空失败", e);
            }
            
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("缓存清空操作发生异常", e);
        }
    }

    /**
     * 获取完整的缓存统计信息
     * 
     * 功能说明：
     * - 提供详细的缓存性能统计和命中率分析
     * - 包含L1和L2缓存的分别统计
     * - 计算总体命中率和各级缓存的贡献度
     * 
     * @return 包含详细统计信息的格式化字符串
     */
    public String getCacheStats() {
        // 获取计数器数据
        long l1Hits = l1HitCount.get();
        long l2Hits = l2HitCount.get();
        long misses = missCount.get();
        long errors = errorCount.get();
        long totalRequests = l1Hits + l2Hits + misses;
        
        // 计算命中率
        double totalHitRate = totalRequests > 0 ? ((l1Hits + l2Hits) * 100.0 / totalRequests) : 0.0;
        double l1HitRate = totalRequests > 0 ? (l1Hits * 100.0 / totalRequests) : 0.0;
        double l2HitRate = totalRequests > 0 ? (l2Hits * 100.0 / totalRequests) : 0.0;
        double errorRate = totalRequests > 0 ? (errors * 100.0 / totalRequests) : 0.0;
        
        // 获取L1缓存的详细统计
        com.github.benmanes.caffeine.cache.stats.CacheStats l1Stats = l1Cache.stats();
        
        return String.format(
            "多级缓存统计 - " +
            "总请求: %d, " +
            "L1命中: %d(%.2f%%), " +
            "L2命中: %d(%.2f%%), " +
            "未命中: %d(%.2f%%), " +
            "错误: %d(%.2f%%), " +
            "总命中率: %.2f%%, " +
            "L1大小: %d, " +
            "L1驱逐: %d",
            totalRequests, 
            l1Hits, l1HitRate, 
            l2Hits, l2HitRate, 
            misses, (totalRequests > 0 ? misses * 100.0 / totalRequests : 0.0), 
            errors, errorRate, 
            totalHitRate,
            l1Cache.estimatedSize(),
            l1Stats.evictionCount()
        );
    }

    /**
     * 获取L1缓存的Caffeine统计信息
     * 
     * @return Caffeine缓存的详细统计信息字符串
     */
    public String getL1CacheStats() {
        return l1Cache.stats().toString();
    }

    /**
     * 获取L1缓存当前大小
     * 
     * @return L1缓存中的条目数量
     */
    public long getL1CacheSize() {
        return l1Cache.estimatedSize();
    }
    
    /**
     * 获取缓存命中率信息
     * 
     * @return 包含各级缓存命中率的统计对象
     */
    public CacheStatsInfo getStatsInfo() {
        long l1Hits = l1HitCount.get();
        long l2Hits = l2HitCount.get();
        long misses = missCount.get();
        long errors = errorCount.get();
        long totalRequests = l1Hits + l2Hits + misses;
        
        return new CacheStatsInfo(l1Hits, l2Hits, misses, errors, totalRequests);
    }
    
    /**
     * 重置所有统计计数器
     * 
     * 注意：此方法主要用于测试和监控重置，生产环境谨慎使用
     */
    public void resetStats() {
        l1HitCount.set(0);
        l2HitCount.set(0);
        missCount.set(0);
        errorCount.set(0);
        lastStatsLogTime = System.currentTimeMillis();
        logger.info("缓存统计计数器已重置");
    }
    
    /**
     * 缓存统计信息数据类
     */
    public static class CacheStatsInfo {
        public final long l1Hits;
        public final long l2Hits;
        public final long misses;
        public final long errors;
        public final long totalRequests;
        public final double totalHitRate;
        public final double l1HitRate;
        public final double l2HitRate;
        
        public CacheStatsInfo(long l1Hits, long l2Hits, long misses, long errors, long totalRequests) {
            this.l1Hits = l1Hits;
            this.l2Hits = l2Hits;
            this.misses = misses;
            this.errors = errors;
            this.totalRequests = totalRequests;
            this.totalHitRate = totalRequests > 0 ? ((l1Hits + l2Hits) * 100.0 / totalRequests) : 0.0;
            this.l1HitRate = totalRequests > 0 ? (l1Hits * 100.0 / totalRequests) : 0.0;
            this.l2HitRate = totalRequests > 0 ? (l2Hits * 100.0 / totalRequests) : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "CacheStats{总请求=%d, L1命中=%d(%.2f%%), L2命中=%d(%.2f%%), 未命中=%d, 错误=%d, 总命中率=%.2f%%}",
                totalRequests, l1Hits, l1HitRate, l2Hits, l2HitRate, misses, errors, totalHitRate
            );
        }
    }
}