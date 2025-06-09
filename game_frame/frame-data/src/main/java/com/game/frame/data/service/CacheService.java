package com.game.frame.data.service;

import com.game.frame.data.cache.MultiLevelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

/**
 * 缓存服务类
 * 
 * 功能说明：
 * - 提供统一的多级缓存操作接口
 * - 封装本地缓存和分布式缓存的复杂性
 * - 支持缓存穿透保护和数据自动加载
 * - 提供缓存统计和监控功能
 * 
 * 设计思路：
 * - 采用多级缓存架构：L1(本地缓存) + L2(Redis缓存)
 * - 实现Cache-Aside模式，业务逻辑控制缓存更新
 * - 提供getOrLoad模式，自动处理缓存未命中情况
 * - 统一的过期时间管理和缓存失效策略
 * 
 * 缓存策略：
 * - 热点数据优先存储在本地缓存
 * - 大数据量使用分布式缓存
 * - 支持主动过期和被动清理
 * - 缓存雪崩和穿透防护
 * 
 * 使用场景：
 * - 游戏配置数据缓存
 * - 玩家基础信息缓存
 * - 排行榜和统计数据缓存
 * - 频繁查询的数据库结果缓存
 * 
 * @author lx
 * @date 2025/06/08
 */
@Service
public class CacheService {

    // 日志记录器，用于记录缓存操作和异常信息
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    // 多级缓存实现，封装本地缓存和分布式缓存的操作
    @Autowired
    private MultiLevelCache multiLevelCache;

    /**
     * 获取缓存值，缓存未命中时通过加载器自动加载数据
     * 
     * 执行流程：
     * 1. 首先尝试从多级缓存中获取数据
     * 2. 如果缓存命中，直接返回缓存值
     * 3. 如果缓存未命中，调用加载器获取原始数据
     * 4. 将加载到的数据存入缓存（仅当数据不为null时）
     * 5. 返回加载到的数据
     * 
     * @param key 缓存键，应该具有业务含义且全局唯一
     * @param loader 数据加载器，通常是从数据库或其他数据源加载数据的逻辑
     * @param <T> 缓存数据的类型
     * @return 缓存值或加载到的数据，加载失败时返回null
     * 
     * 注意事项：
     * - 加载器执行期间可能阻塞当前线程
     * - 建议为加载器设置合理的超时时间
     * - 不会缓存null值，避免缓存穿透
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Callable<T> loader) {
        T value = multiLevelCache.get(key);
        if (value != null) {
            return value;
        }

        try {
            // 调用加载器获取原始数据
            value = loader.call();
            if (value != null) {
                // 仅缓存非null值，防止缓存穿透
                multiLevelCache.put(key, value);
            }
            return value;
        } catch (Exception e) {
            logger.error("数据加载失败，缓存键: {}", key, e);
            return null;
        }
    }

    /**
     * 获取缓存值，支持自定义过期时间的数据加载
     * 
     * 与基础版本的区别：
     * - 支持为新加载的数据设置特定的过期时间
     * - 适用于具有时效性要求的业务数据
     * - 可以根据数据重要性设置不同的缓存时长
     * 
     * @param key 缓存键，应该具有业务含义且全局唯一
     * @param loader 数据加载器，用于获取原始数据
     * @param expireSeconds 缓存过期时间（秒），0表示永不过期
     * @param <T> 缓存数据的类型
     * @return 缓存值或加载到的数据，加载失败时返回null
     * 
     * 使用场景：
     * - 配置数据：长期缓存（如1小时）
     * - 玩家数据：中期缓存（如10分钟）
     * - 实时数据：短期缓存（如30秒）
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
                // 使用指定的过期时间存储数据
                multiLevelCache.put(key, value, expireSeconds);
            }
            return value;
        } catch (Exception e) {
            logger.error("带过期时间的数据加载失败，缓存键: {}", key, e);
            return null;
        }
    }

    /**
     * 设置缓存数据（使用默认过期策略）
     * 
     * @param key 缓存键，建议使用模块前缀避免键冲突，如"user:123"
     * @param value 要缓存的数据对象，不建议缓存null值
     * 
     * 注意事项：
     * - 使用多级缓存的默认过期时间
     * - 数据会同时存储在本地缓存和分布式缓存中
     */
    public void put(String key, Object value) {
        multiLevelCache.put(key, value);
    }

    /**
     * 设置缓存数据（指定过期时间）
     * 
     * @param key 缓存键，建议使用有意义的命名规范
     * @param value 要缓存的数据对象
     * @param expireSeconds 过期时间（秒），0表示永不过期
     * 
     * 过期时间建议：
     * - 静态配置数据：3600秒（1小时）
     * - 用户会话数据：1800秒（30分钟）
     * - 临时计算结果：300秒（5分钟）
     */
    public void put(String key, Object value, long expireSeconds) {
        multiLevelCache.put(key, value, expireSeconds);
    }

    /**
     * 获取缓存数据
     * 
     * @param key 缓存键
     * @param <T> 返回数据的类型
     * @return 缓存中的数据，不存在或已过期时返回null
     * 
     * 查找顺序：
     * 1. L1本地缓存（速度最快）
     * 2. L2分布式缓存（网络开销）
     * 3. 如果都未命中返回null
     */
    public <T> T get(String key) {
        return multiLevelCache.get(key);
    }

    /**
     * 删除指定的缓存条目
     * 
     * @param key 要删除的缓存键
     * 
     * 执行逻辑：
     * - 同时从本地缓存和分布式缓存中删除数据
     * - 确保数据一致性，避免脏数据
     * - 适用于数据更新后的缓存失效场景
     * 
     * 使用场景：
     * - 用户信息更新后清除旧缓存
     * - 配置变更后强制刷新
     * - 定期清理过期或无效数据
     */
    public void remove(String key) {
        multiLevelCache.remove(key);
    }

    /**
     * 检查指定键的缓存是否存在
     * 
     * @param key 要检查的缓存键
     * @return true表示缓存存在，false表示不存在或已过期
     * 
     * 注意事项：
     * - 此方法只检查键是否存在，不返回实际值
     * - 比get方法性能略好，适用于存在性检查
     */
    public boolean exists(String key) {
        return multiLevelCache.exists(key);
    }

    /**
     * 清空所有缓存数据
     * 
     * 危险操作警告：
     * - 会清除所有业务缓存数据
     * - 可能导致短期内数据库压力增大
     * - 建议仅在维护或紧急情况下使用
     * 
     * 适用场景：
     * - 系统维护时的数据清理
     * - 缓存污染后的紧急恢复
     * - 测试环境的数据重置
     */
    public void clear() {
        multiLevelCache.clear();
    }

    /**
     * 获取缓存系统的统计信息
     * 
     * @return 包含命中率、访问次数等信息的统计字符串
     * 
     * 统计内容可能包括：
     * - 缓存命中率和未命中率
     * - 总访问次数和成功次数
     * - 缓存大小和内存使用情况
     * - 过期和清理操作统计
     * 
     * 用途：
     * - 性能监控和优化分析
     * - 缓存效果评估
     * - 系统健康状态检查
     */
    public String getStats() {
        return multiLevelCache.getL1CacheStats();
    }

    /**
     * 获取当前缓存的条目数量
     * 
     * @return 缓存中存储的条目总数
     * 
     * 说明：
     * - 返回L1本地缓存的大小
     * - 不包括已过期但未清理的条目
     * - 可用于监控缓存使用情况和内存预警
     */
    public long getSize() {
        return multiLevelCache.getL1CacheSize();
    }
}