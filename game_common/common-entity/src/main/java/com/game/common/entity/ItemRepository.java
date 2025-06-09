package com.game.common.entity;

import com.game.frame.data.repository.CacheableRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 物品数据访问
 * @author lx
 * @date 2025/06/08
 */
@Repository
public interface ItemRepository extends CacheableRepository<ItemEntity, Long> {

    /**
     * 根据用户ID查找物品
     * @param userId 用户ID
     * @return 物品列表
     */
    List<ItemEntity> findByUserIdAndDeletedEquals(Long userId, Integer deleted);

    /**
     * 根据用户ID查找物品（活跃物品）
     * @param userId 用户ID
     * @return 物品列表
     */
    default List<ItemEntity> findByUserId(Long userId) {
        return findByUserIdAndDeletedEquals(userId, 0);
    }

    /**
     * 根据物品类型查找
     * @param itemType 物品类型
     * @return 物品列表
     */
    List<ItemEntity> findByItemTypeAndDeletedEquals(String itemType, Integer deleted);

    /**
     * 根据用户ID和物品类型查找
     * @param userId 用户ID
     * @param itemType 物品类型
     * @return 物品列表
     */
    List<ItemEntity> findByUserIdAndItemTypeAndDeletedEquals(Long userId, String itemType, Integer deleted);

    /**
     * 根据用户ID和物品ID查找
     * @param userId 用户ID
     * @param itemId 物品ID
     * @return 物品列表
     */
    List<ItemEntity> findByUserIdAndItemIdAndDeletedEquals(Long userId, String itemId, Integer deleted);

    /**
     * 统计用户物品总数
     * @param userId 用户ID
     * @return 物品总数
     */
    @Query("SELECT SUM(i.quantity) FROM ItemEntity i WHERE i.userId = :userId AND i.deleted = 0")
    Long sumQuantityByUserId(@Param("userId") Long userId);

    /**
     * 统计指定类型物品总数
     * @param userId 用户ID
     * @param itemType 物品类型
     * @return 物品总数
     */
    @Query("SELECT SUM(i.quantity) FROM ItemEntity i WHERE i.userId = :userId AND i.itemType = :itemType AND i.deleted = 0")
    Long sumQuantityByUserIdAndItemType(@Param("userId") Long userId, @Param("itemType") String itemType);

    /**
     * 根据分片键查找物品
     * @param shardingKey 分片键
     * @return 物品列表
     */
    List<ItemEntity> findByShardingKeyAndDeletedEquals(String shardingKey, Integer deleted);
}