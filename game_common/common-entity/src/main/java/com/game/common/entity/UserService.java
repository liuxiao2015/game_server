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
 * 用户服务类
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