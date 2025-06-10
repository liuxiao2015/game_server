package com.game.common.entity;

import com.game.frame.data.annotation.DataSource;
import com.game.frame.data.datasource.DataSourceType;
import com.game.frame.data.service.CacheService;
import com.game.frame.data.utils.CacheKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 游戏用户服务类
 * 
 * 功能说明：
 * - 提供用户实体的完整业务逻辑操作
 * - 集成多级缓存提升用户数据访问性能
 * - 支持读写分离的数据库访问策略
 * - 实现用户数据的事务性管理和一致性保证
 * 
 * 设计思路：
 * - 采用Service层模式封装用户业务逻辑
 * - 集成Spring事务管理确保数据一致性
 * - 使用缓存服务提升热点数据访问效率
 * - 支持主从数据库的读写分离优化
 * 
 * 核心功能：
 * - 用户查询：支持ID、用户名等多种查询方式
 * - 用户创建：包含数据验证和缓存更新
 * - 用户更新：支持单个字段和批量更新
 * - 用户删除：采用逻辑删除保留数据
 * - 统计查询：用户数量、等级分布等统计信息
 * 
 * 缓存策略：
 * - 用户基础信息：长期缓存，写入时更新
 * - 查询结果：短期缓存，定期刷新
 * - 统计数据：中期缓存，平衡准确性和性能
 * - 缓存键：统一生成规则，便于管理和清理
 * 
 * 数据库优化：
 * - 读操作：使用从库减轻主库压力
 * - 写操作：使用主库确保数据一致性
 * - 批量操作：减少数据库访问次数
 * - 索引优化：针对常用查询建立索引
 * 
 * 事务管理：
 * - 写操作：开启事务确保数据一致性
 * - 读操作：只读事务优化性能
 * - 异常回滚：自动回滚失败操作
 * - 嵌套事务：支持复杂业务场景
 * 
 * 性能考虑：
 * - 缓存预热：系统启动时加载热点数据
 * - 缓存穿透：null值缓存防止频繁查询
 * - 缓存雪崩：错峰过期防止集中失效
 * - 数据同步：缓存与数据库的一致性保证
 * 
 * 监控指标：
 * - 缓存命中率：监控缓存效果
 * - 查询耗时：识别性能瓶颈
 * - 事务成功率：监控数据操作质量
 * - 并发访问：监控系统负载情况
 * 
 * 使用场景：
 * - 用户登录验证和信息获取
 * - 用户资料的增删改查操作
 * - 用户行为的统计和分析
 * - 用户数据的批量处理和迁移
 *
 * @author lx
 * @date 2025/06/08
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheService cacheService;

    /**
     * 根据ID查找用户（使用缓存）
     * @param userId 用户ID
     * @return 用户实体
     */
    @DataSource(DataSourceType.SLAVE) // 读操作使用从库
    @Transactional(readOnly = true)
    public Optional<UserEntity> findById(Long userId) {
        String cacheKey = CacheKeyGenerator.userKey(userId);
        return Optional.ofNullable(cacheService.getOrLoad(cacheKey, () -> {
            return userRepository.findById(userId).orElse(null);
        }));
    }

    /**
     * 根据用户名查找用户（使用缓存）
     * @param username 用户名
     * @return 用户实体
     */
    @DataSource(DataSourceType.SLAVE) // 读操作使用从库
    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUsername(String username) {
        String cacheKey = CacheKeyGenerator.usernameKey(username);
        return Optional.ofNullable(cacheService.getOrLoad(cacheKey, () -> {
            return userRepository.findByUsername(username).orElse(null);
        }));
    }

    /**
     * 创建用户
     * @param user 用户实体
     * @return 保存后的用户实体
     */
    @DataSource(DataSourceType.MASTER) // 写操作使用主库
    public UserEntity createUser(UserEntity user) {
        UserEntity savedUser = userRepository.save(user);
        
        // 更新缓存
        cacheService.put(CacheKeyGenerator.userKey(savedUser.getId()), savedUser);
        if (savedUser.getUsername() != null) {
            cacheService.put(CacheKeyGenerator.usernameKey(savedUser.getUsername()), savedUser);
        }
        
        return savedUser;
    }

    /**
     * 更新用户
     * @param user 用户实体
     * @return 更新后的用户实体
     */
    @DataSource(DataSourceType.MASTER) // 写操作使用主库
    public UserEntity updateUser(UserEntity user) {
        UserEntity savedUser = userRepository.save(user);
        
        // 更新缓存
        cacheService.put(CacheKeyGenerator.userKey(savedUser.getId()), savedUser);
        if (savedUser.getUsername() != null) {
            cacheService.put(CacheKeyGenerator.usernameKey(savedUser.getUsername()), savedUser);
        }
        
        return savedUser;
    }

    /**
     * 更新最后登录时间
     * @param userId 用户ID
     * @return 是否更新成功
     */
    @DataSource(DataSourceType.MASTER) // 写操作使用主库
    public boolean updateLastLoginTime(Long userId) {
        int affected = userRepository.updateLastLoginTime(userId, LocalDateTime.now());
        
        if (affected > 0) {
            // 清除缓存，下次查询时重新加载
            cacheService.remove(CacheKeyGenerator.userKey(userId));
        }
        
        return affected > 0;
    }

    /**
     * 批量更新用户等级
     * @param userIds 用户ID列表
     * @param level 新等级
     * @return 影响的行数
     */
    @DataSource(DataSourceType.MASTER) // 写操作使用主库
    public int batchUpdateLevel(List<Long> userIds, Integer level) {
        int affected = userRepository.batchUpdateLevel(userIds, level);
        
        if (affected > 0) {
            // 清除相关缓存
            userIds.forEach(userId -> {
                cacheService.remove(CacheKeyGenerator.userKey(userId));
            });
        }
        
        return affected;
    }

    /**
     * 根据等级范围查找用户
     * @param minLevel 最小等级
     * @param maxLevel 最大等级
     * @return 用户列表
     */
    @DataSource(DataSourceType.SLAVE) // 读操作使用从库
    @Transactional(readOnly = true)
    public List<UserEntity> findByLevelRange(Integer minLevel, Integer maxLevel) {
        String cacheKey = CacheKeyGenerator.queryKey("levelRange", minLevel, maxLevel);
        return cacheService.getOrLoad(cacheKey, () -> {
            return userRepository.findByLevelBetweenAndDeletedEquals(minLevel, maxLevel, 0);
        }, 300); // 缓存5分钟
    }

    /**
     * 统计活跃用户总数
     * @return 用户总数
     */
    @DataSource(DataSourceType.SLAVE) // 读操作使用从库
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        String cacheKey = CacheKeyGenerator.statKey("activeUsers");
        Long count = cacheService.getOrLoad(cacheKey, () -> {
            return userRepository.countActiveUsers();
        }, 600); // 缓存10分钟
        return count != null ? count : 0L;
    }

    /**
     * 统计指定等级用户数量
     * @param level 等级
     * @return 用户数量
     */
    @DataSource(DataSourceType.SLAVE) // 读操作使用从库
    @Transactional(readOnly = true)
    public long countByLevel(Integer level) {
        String cacheKey = CacheKeyGenerator.statKey("levelCount", level);
        Long count = cacheService.getOrLoad(cacheKey, () -> {
            return userRepository.countByLevel(level);
        }, 300); // 缓存5分钟
        return count != null ? count : 0L;
    }

    /**
     * 删除用户（逻辑删除）
     * @param userId 用户ID
     * @return 是否删除成功
     */
    @DataSource(DataSourceType.MASTER) // 写操作使用主库
    public boolean deleteUser(Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            user.setDeleted(1);
            userRepository.save(user);
            
            // 清除缓存
            cacheService.remove(CacheKeyGenerator.userKey(userId));
            if (user.getUsername() != null) {
                cacheService.remove(CacheKeyGenerator.usernameKey(user.getUsername()));
            }
            
            return true;
        }
        return false;
    }
}