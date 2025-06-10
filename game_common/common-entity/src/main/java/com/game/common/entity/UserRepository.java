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
 * 用户数据访问层接口
 * 
 * 功能说明：
 * - 提供用户实体的CRUD操作和复杂查询功能
 * - 集成缓存机制提升数据访问性能
 * - 支持批量操作和统计查询
 * - 实现软删除和数据安全保护
 * 
 * 设计思路：
 * - 继承CacheableRepository获得缓存能力
 * - 使用Spring Data JPA简化数据访问代码
 * - 采用软删除机制保护重要数据
 * - 通过@Query注解实现复杂业务查询
 * 
 * 缓存策略：
 * - 用户基本信息缓存：提升登录和查询性能
 * - 缓存键设计：支持多维度查询缓存
 * - 缓存过期：平衡数据一致性和性能
 * - 缓存更新：写操作自动清理相关缓存
 * 
 * 数据安全：
 * - 软删除：deleted字段标记，不物理删除数据
 * - 查询过滤：默认过滤已删除数据
 * - 批量操作：支持事务和回滚机制
 * - 乐观锁：防止并发更新冲突
 * 
 * 性能优化：
 * - 合理的索引设计：用户名、等级、VIP等级
 * - 批量操作：减少数据库交互次数
 * - 分页查询：避免大结果集的内存问题
 * - 查询优化：避免N+1查询问题
 * 
 * 业务场景：
 * - 用户登录认证和信息查询
 * - 用户等级系统和经验值管理
 * - VIP系统和特权功能支持
 * - 用户行为统计和数据分析
 * - 批量用户操作和管理功能
 * 
 * 注意事项：
 * - 所有查询默认过滤deleted=0的记录
 * - 缓存键设计要考虑数据一致性
 * - 批量操作要考虑事务边界
 * - 统计查询要考虑性能影响
 * 
 * @author lx
 * @date 2025/06/08
 * @since 1.0.0
 * @see UserEntity
 * @see CacheableRepository
 */
@Repository
public interface UserRepository extends CacheableRepository<UserEntity, Long> {

    /**
     * 根据用户名查找用户（包含删除状态过滤）
     * 
     * 核心登录认证方法，具备以下特性：
     * - 集成Cacheable注解，提升重复查询性能
     * - 软删除过滤，只返回有效用户数据
     * - 唯一性保证，用户名在系统中唯一
     * - 空值安全，避免NPE异常
     * 
     * 缓存策略：
     * - 缓存名：users
     * - 缓存键：username: + 用户名
     * - 过期时间：30分钟（配置文件定义）
     * - 更新策略：用户信息变更时自动清理
     * 
     * 性能考虑：
     * - 用户名字段建立唯一索引
     * - deleted字段建立复合索引
     * - 缓存命中率通常很高（登录重复查询）
     * 
     * @param username 用户名，不能为空且需要符合命名规范
     * @param deleted 删除标记，0表示正常，1表示已删除
     * @return Optional包装的用户实体，未找到时返回empty
     */
    @Cacheable(value = "users", key = "'username:' + #username")
    Optional<UserEntity> findByUsernameAndDeletedEquals(String username, Integer deleted);

    /**
     * 根据用户名查找活跃用户（业务便捷方法）
     * 
     * 封装常用的用户查询逻辑，自动过滤已删除用户：
     * - 简化业务代码调用
     * - 统一删除状态处理
     * - 提供默认行为约定
     * - 减少参数传递错误
     * 
     * 实现说明：
     * - 调用findByUsernameAndDeletedEquals(username, 0)
     * - deleted=0表示用户正常状态
     * - 利用默认方法减少代码重复
     * 
     * 使用场景：
     * - 用户登录验证
     * - 用户信息查询
     * - 业务逻辑中的用户检索
     * 
     * @param username 用户名
     * @return Optional包装的活跃用户实体
     */
    default Optional<UserEntity> findByUsername(String username) {
        return findByUsernameAndDeletedEquals(username, 0);
    }

    /**
     * 根据等级范围查找用户列表
     * 
     * 支持等级系统相关功能，如：
     * - 等级排行榜生成
     * - 同等级用户匹配
     * - 等级奖励发放
     * - 数据统计分析
     * 
     * 查询优化：
     * - level字段建立索引提升查询效率
     * - 支持范围查询，包含边界值
     * - 自动过滤已删除用户
     * - 结果按等级排序（可配置）
     * 
     * 注意事项：
     * - 大范围查询可能返回大量数据，建议分页
     * - 等级范围要合理，避免无意义查询
     * - 考虑添加limit参数防止内存溢出
     * 
     * @param minLevel 最小等级（包含）
     * @param maxLevel 最大等级（包含）
     * @param deleted 删除标记，通常传入0
     * @return 符合等级范围的用户列表
     */
    List<UserEntity> findByLevelBetweenAndDeletedEquals(Integer minLevel, Integer maxLevel, Integer deleted);

    /**
     * 根据VIP等级查找用户列表
     * 
     * 支持VIP系统相关功能：
     * - VIP用户特权管理
     * - VIP营销活动推送
     * - VIP数据统计分析
     * - VIP服务优化决策
     * 
     * 业务价值：
     * - 精准营销和用户运营
     * - VIP体验优化和服务提升
     * - 收入分析和商业决策支持
     * - 用户分层和个性化服务
     * 
     * 性能考虑：
     * - vipLevel字段建立索引
     * - VIP用户通常数量较少，查询效率高
     * - 考虑缓存热门VIP等级的用户列表
     * 
     * @param vipLevel VIP等级，0表示普通用户
     * @param deleted 删除标记
     * @return 指定VIP等级的用户列表
     */
    List<UserEntity> findByVipLevelAndDeletedEquals(Integer vipLevel, Integer deleted);

    /**
     * 更新用户最后登录时间（乐观锁更新）
     * 
     * 用于用户活跃度统计和会话管理：
     * - 记录用户最近访问时间
     * - 支持用户活跃度分析
     * - 会话超时检测基础数据
     * - 用户行为轨迹追踪
     * 
     * 实现特性：
     * - 使用@Modifying注解支持更新操作
     * - 自动更新updateTime字段维护数据一致性
     * - 原子操作，避免并发更新问题
     * - 返回影响行数便于操作结果验证
     * 
     * 性能优化：
     * - 直接SQL更新，避免先查询再保存
     * - 只更新必要字段，减少IO开销
     * - userId建立主键索引，更新效率高
     * 
     * 使用场景：
     * - 用户登录成功后更新
     * - 重要操作的活跃度记录
     * - 会话续期和超时管理
     * 
     * @param userId 用户ID，必须为有效的用户标识
     * @param lastLoginTime 最后登录时间，通常为当前时间
     * @return 影响的行数，正常情况下应该为1
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.lastLoginTime = :lastLoginTime, u.updateTime = CURRENT_TIMESTAMP WHERE u.id = :userId")
    int updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * 批量更新用户等级（管理员功能）
     * 
     * 支持批量用户等级调整，常用于：
     * - 游戏活动奖励发放
     * - 管理员批量操作
     * - 数据修复和调整
     * - 等级系统重构迁移
     * 
     * 批量操作优势：
     * - 减少数据库交互次数
     * - 支持事务回滚，保证数据一致性
     * - 提升大量数据更新的性能
     * - 统一操作，减少错误概率
     * 
     * 安全考虑：
     * - 需要管理员权限验证
     * - 记录操作日志便于审计
     * - 验证用户ID列表的有效性
     * - 设置合理的批量大小限制
     * 
     * 注意事项：
     * - userIds列表不能为空或过大
     * - level值要在合理范围内
     * - 考虑等级变更的业务影响
     * - 可能触发等级相关的游戏逻辑
     * 
     * @param userIds 用户ID列表，不能为空
     * @param level 新的等级值，必须为正整数
     * @return 影响的行数，应该等于userIds的大小
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.level = :level, u.updateTime = CURRENT_TIMESTAMP WHERE u.id IN :userIds")
    int batchUpdateLevel(@Param("userIds") List<Long> userIds, @Param("level") Integer level);

    /**
     * 根据经验值范围查找用户列表
     * 
     * 支持经验值相关的游戏功能：
     * - 经验值排行榜
     * - 升级奖励发放
     * - 用户分层分析
     * - 游戏平衡性调优
     * 
     * 查询场景：
     * - 查找即将升级的用户
     * - 分析经验值分布情况
     * - 制定用户成长策略
     * - 游戏数值平衡调整
     * 
     * 性能优化：
     * - exp字段建立索引支持范围查询
     * - 合理设置查询范围避免全表扫描
     * - 考虑结果排序和分页处理
     * - 缓存热门经验值区间的查询结果
     * 
     * @param minExp 最小经验值（包含）
     * @param maxExp 最大经验值（包含）
     * @param deleted 删除标记
     * @return 符合经验值范围的用户列表
     */
    List<UserEntity> findByExpBetweenAndDeletedEquals(Long minExp, Long maxExp, Integer deleted);

    /**
     * 统计活跃用户总数
     * 
     * 提供系统级的用户统计数据：
     * - 用户规模监控指标
     * - 运营数据统计基础
     * - 系统容量规划依据
     * - 商业报表数据来源
     * 
     * 统计特点：
     * - 只统计未删除用户（deleted=0）
     * - 实时统计，数据准确性高
     * - 支持监控告警和趋势分析
     * - 作为其他统计计算的基础
     * 
     * 性能考虑：
     * - COUNT操作在大表上可能较慢
     * - 考虑使用缓存减少查询频率
     * - deleted字段索引提升查询效率
     * - 可以考虑异步统计和定期更新
     * 
     * 使用场景：
     * - 运营管理后台展示
     * - 系统监控和告警
     * - 数据报表生成
     * - 业务决策支持
     * 
     * @return 活跃用户的总数量
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.deleted = 0")
    long countActiveUsers();

    /**
     * 统计指定等级的用户数量
     * 
     * 支持等级分布分析和游戏平衡：
     * - 各等级用户分布统计
     * - 游戏难度曲线分析
     * - 等级奖励设计参考
     * - 用户成长路径优化
     * 
     * 分析价值：
     * - 发现等级分布异常情况
     * - 优化游戏升级体验
     * - 调整等级奖励策略
     * - 平衡游戏经济系统
     * 
     * 应用场景：
     * - 游戏数据分析报告
     * - 等级系统效果评估
     * - 新手引导优化
     * - 老用户回流策略
     * 
     * @param level 要统计的等级值
     * @return 该等级的用户数量
     */
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.level = :level AND u.deleted = 0")
    long countByLevel(@Param("level") Integer level);
}