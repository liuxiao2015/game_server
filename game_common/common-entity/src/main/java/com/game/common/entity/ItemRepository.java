package com.game.common.entity;

import com.game.frame.data.repository.CacheableRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 游戏物品数据访问Repository
 * 
 * 功能说明：
 * - 提供游戏物品的完整数据访问接口，支持复杂查询和统计操作
 * - 继承CacheableRepository获得自动缓存能力，提升查询性能
 * - 支持多维度的物品查询：用户、类型、ID等组合查询
 * - 提供统计功能，支持物品数量的汇总和分析
 * 
 * 设计思路：
 * - 基于Spring Data JPA的方法命名规范自动生成查询
 * - 所有查询都考虑逻辑删除状态，确保数据一致性
 * - 提供便捷的默认方法，简化常用查询操作
 * - 使用@Query注解实现复杂的统计查询
 * 
 * 查询优化：
 * - 所有查询都包含deleted条件，利用索引优化
 * - 基于用户ID的查询利用分片策略，查询性能优异
 * - 缓存热点数据，减少数据库访问压力
 * - 支持批量查询和聚合统计操作
 * 
 * 使用场景：
 * - 背包系统：查询玩家所有物品或特定类型物品
 * - 装备系统：查询玩家的装备和装备强化数据
 * - 道具系统：管理消耗品、材料等道具物品
 * - 交易系统：物品转移和交易验证
 * - 统计系统：物品数量统计和数据分析
 * 
 * 技术特点：
 * - 自动缓存：继承缓存功能，提升重复查询性能
 * - 分片支持：基于分片键的查询，支持水平扩展
 * - 统计查询：提供SUM等聚合查询，支持数据分析
 * - 类型安全：强类型接口，编译时检查查询参数
 * 
 * 缓存策略：
 * - 用户物品列表：高频查询，适合缓存
 * - 物品统计数据：计算开销大，缓存效果显著
 * - 实时性要求：写操作自动更新缓存，保证一致性
 * 
 * @author lx
 * @date 2025/06/08
 */
@Repository
public interface ItemRepository extends CacheableRepository<ItemEntity, Long> {

    /**
     * 根据用户ID和删除状态查找物品
     * 
     * 功能说明：
     * - 查询指定用户的所有物品，支持按删除状态筛选
     * - 基于用户ID查询，利用分片优化查询性能
     * - 返回完整的物品列表，适用于背包展示等场景
     * 
     * 查询优化：
     * - 使用复合索引(user_id, deleted)提升查询性能
     * - 利用分片策略，查询数据集中在单个分片
     * - 支持缓存，减少重复查询的数据库访问
     * 
     * @param userId 用户ID，不能为null
     * @param deleted 删除状态，0-正常，1-已删除
     * @return List<ItemEntity> 物品列表，可能为空
     */
    List<ItemEntity> findByUserIdAndDeletedEquals(Long userId, Integer deleted);

    /**
     * 查找用户的有效物品
     * 
     * 功能说明：
     * - 便捷方法，查询用户所有未删除的物品
     * - 最常用的查询方式，用于背包显示和物品管理
     * - 默认方法实现，调用带删除状态的查询方法
     * 
     * 业务逻辑：
     * - 只返回有效物品(deleted=0)
     * - 适用于前端展示和游戏逻辑处理
     * - 简化调用方代码，无需每次指定删除状态
     * 
     * @param userId 用户ID，不能为null
     * @return List<ItemEntity> 有效物品列表
     */
    default List<ItemEntity> findByUserId(Long userId) {
        return findByUserIdAndDeletedEquals(userId, 0);
    }

    /**
     * 根据物品类型查找物品
     * 
     * 功能说明：
     * - 按物品类型查询，支持按删除状态筛选
     * - 用于管理特定类型的物品，如装备、道具等
     * - 跨用户查询，适用于游戏管理和统计场景
     * 
     * 使用场景：
     * - 管理员查询特定类型的所有物品
     * - 游戏统计特定类型物品的分布
     * - 批量处理某类物品的业务操作
     * 
     * @param itemType 物品类型，如"equipment"、"consumable"等
     * @param deleted 删除状态，0-正常，1-已删除
     * @return List<ItemEntity> 物品列表
     */
    List<ItemEntity> findByItemTypeAndDeletedEquals(String itemType, Integer deleted);

    /**
     * 根据用户ID和物品类型查找物品
     * 
     * 功能说明：
     * - 查询特定用户的特定类型物品
     * - 组合查询，提供更精确的数据筛选
     * - 支持按删除状态筛选，确保数据准确性
     * 
     * 使用场景：
     * - 查询玩家的所有装备或所有道具
     * - 背包分类显示，按类型组织物品
     * - 特定类型物品的管理和操作
     * 
     * 性能优化：
     * - 利用复合索引(user_id, item_type, deleted)
     * - 分片查询，数据集中在单个分片
     * 
     * @param userId 用户ID
     * @param itemType 物品类型
     * @param deleted 删除状态
     * @return List<ItemEntity> 物品列表
     */
    List<ItemEntity> findByUserIdAndItemTypeAndDeletedEquals(Long userId, String itemType, Integer deleted);

    /**
     * 根据用户ID和物品ID查找物品
     * 
     * 功能说明：
     * - 查询特定用户的特定物品
     * - 精确查询，通常用于物品操作和验证
     * - 支持查找同一物品的多个实例(如堆叠物品)
     * 
     * 使用场景：
     * - 物品使用前的存在性验证
     * - 物品强化、升级等操作
     * - 交易系统中的物品验证
     * - 物品合成和分解操作
     * 
     * @param userId 用户ID
     * @param itemId 物品配置ID
     * @param deleted 删除状态
     * @return List<ItemEntity> 物品列表，可能包含多个相同物品
     */
    List<ItemEntity> findByUserIdAndItemIdAndDeletedEquals(Long userId, String itemId, Integer deleted);

    /**
     * 统计用户物品总数量
     * 
     * 功能说明：
     * - 计算用户所有物品的数量总和
     * - 使用JPQL聚合查询，高效计算总数
     * - 只统计有效物品，排除已删除物品
     * 
     * 业务价值：
     * - 背包容量检查：验证是否超出背包上限
     * - 用户资产统计：计算用户拥有的物品总量
     * - 游戏平衡分析：了解物品产出和消耗情况
     * 
     * 性能考虑：
     * - 使用数据库聚合函数，避免在应用层计算
     * - 结果适合缓存，减少重复计算
     * - 查询只涉及单个用户，利用分片优化
     * 
     * @param userId 用户ID
     * @return Long 物品总数量，null表示用户无物品
     */
    @Query("SELECT SUM(i.quantity) FROM ItemEntity i WHERE i.userId = :userId AND i.deleted = 0")
    Long sumQuantityByUserId(@Param("userId") Long userId);

    /**
     * 统计用户特定类型物品总数量
     * 
     * 功能说明：
     * - 计算用户特定类型物品的数量总和
     * - 提供更精细的物品统计功能
     * - 支持按类型的容量限制和管理
     * 
     * 使用场景：
     * - 装备栏位检查：统计已装备的装备数量
     * - 材料包检查：统计制作材料的数量
     * - 类型限制：检查特定类型物品是否超限
     * 
     * 统计维度：
     * - 按用户分组：每个用户独立统计
     * - 按类型筛选：只统计指定类型物品
     * - 排除删除：只统计有效物品
     * 
     * @param userId 用户ID
     * @param itemType 物品类型
     * @return Long 指定类型物品的总数量
     */
    @Query("SELECT SUM(i.quantity) FROM ItemEntity i WHERE i.userId = :userId AND i.itemType = :itemType AND i.deleted = 0")
    Long sumQuantityByUserIdAndItemType(@Param("userId") Long userId, @Param("itemType") String itemType);

    /**
     * 根据分片键查找物品
     * 
     * 功能说明：
     * - 基于分片键进行查询，支持分库分表场景
     * - 用于跨用户的分片级查询和数据维护
     * - 支持数据迁移和分片重平衡操作
     * 
     * 使用场景：
     * - 数据库运维：按分片进行数据统计和维护
     * - 数据迁移：分片间的数据转移
     * - 性能监控：监控各分片的数据分布
     * - 故障恢复：特定分片的数据恢复
     * 
     * 分片逻辑：
     * - 分片键通常是用户ID的字符串形式
     * - 同一分片键的所有数据在同一个分片
     * - 便于分片级的批量操作
     * 
     * @param shardingKey 分片键，通常是用户ID的字符串形式
     * @param deleted 删除状态
     * @return List<ItemEntity> 该分片的物品列表
     */
    List<ItemEntity> findByShardingKeyAndDeletedEquals(String shardingKey, Integer deleted);
}