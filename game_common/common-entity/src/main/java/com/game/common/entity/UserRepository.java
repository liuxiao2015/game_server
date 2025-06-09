package com.game.common.entity;

import com.game.frame.data.repository.CacheableRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问
 * @author lx
 * @date 2025/06/08
 */
@Repository
public interface UserRepository extends CacheableRepository<UserEntity, Long> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体
     */
    @Cacheable(value = "users", key = "'username:' + #username")
    Optional<UserEntity> findByUsernameAndDeletedEquals(String username, Integer deleted);

    /**
     * 根据用户名查找用户（活跃用户）
     * @param username 用户名
     * @return 用户实体
     */
    default Optional<UserEntity> findByUsername(String username) {
        return findByUsernameAndDeletedEquals(username, 0);
    }

    /**
     * 根据等级范围查找用户
     * @param minLevel 最小等级
     * @param maxLevel 最大等级
     * @return 用户列表
     */
    List<UserEntity> findByLevelBetweenAndDeletedEquals(Integer minLevel, Integer maxLevel, Integer deleted);

    /**
     * 根据VIP等级查找用户
     * @param vipLevel VIP等级
     * @return 用户列表
     */
    List<UserEntity> findByVipLevelAndDeletedEquals(Integer vipLevel, Integer deleted);

    /**
     * 更新最后登录时间
     * @param userId 用户ID
     * @param lastLoginTime 最后登录时间
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.lastLoginTime = :lastLoginTime, u.updateTime = CURRENT_TIMESTAMP WHERE u.id = :userId")
    int updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * 批量更新用户等级
     * @param userIds 用户ID列表
     * @param level 新等级
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.level = :level, u.updateTime = CURRENT_TIMESTAMP WHERE u.id IN :userIds")
    int batchUpdateLevel(@Param("userIds") List<Long> userIds, @Param("level") Integer level);

    /**
     * 根据经验值范围查找用户
     * @param minExp 最小经验值
     * @param maxExp 最大经验值
     * @return 用户列表
     */
    List<UserEntity> findByExpBetweenAndDeletedEquals(Long minExp, Long maxExp, Integer deleted);

    /**
     * 统计用户总数（活跃用户）
     * @return 用户总数
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.deleted = 0")
    long countActiveUsers();

    /**
     * 统计指定等级的用户数量
     * @param level 等级
     * @return 用户数量
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.level = :level AND u.deleted = 0")
    long countByLevel(@Param("level") Integer level);
}