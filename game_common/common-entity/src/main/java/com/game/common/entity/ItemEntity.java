package com.game.common.entity;

import com.game.frame.data.entity.ShardingEntity;
import jakarta.persistence.*;

/**
 * 游戏物品实体类
 * 
 * 功能说明：
 * - 表示游戏中的各种物品，包括装备、道具、材料等
 * - 支持物品的持久化存储和分片存储机制
 * - 提供物品属性的JSON扩展存储，支持灵活的属性配置
 * - 集成分库分表功能，基于用户ID进行数据分片
 * 
 * 设计思路：
 * - 继承ShardingEntity获得分片存储能力
 * - 使用JSON字段存储动态属性，避免频繁修改表结构
 * - 建立合适的数据库索引，优化查询性能
 * - 自动设置分片键，简化分片逻辑
 * 
 * 数据模型：
 * - 每个物品属于特定用户，通过userId关联
 * - itemType分类物品类型：装备、道具、材料等
 * - itemId标识具体物品，对应配置表中的物品ID
 * - quantity记录物品数量，支持可堆叠物品
 * - properties存储扩展属性，如装备强化等级、宝石等
 * 
 * 使用场景：
 * - 玩家背包系统：存储玩家拥有的所有物品
 * - 装备系统：管理玩家装备的属性和状态
 * - 道具系统：记录消耗品和任务物品
 * - 交易系统：物品转移和交易记录
 * 
 * 技术特点：
 * - 分片存储：基于用户ID分片，支持水平扩展
 * - 索引优化：对常用查询字段建立索引
 * - JSON扩展：动态属性存储，适应游戏内容变化
 * - 自动分片：设置用户ID时自动设置分片键
 * 
 * 分片策略：
 * - 分片键：基于用户ID进行分片路由
 * - 查询优化：大多数查询都包含用户ID，分片效果好
 * - 数据隔离：不同用户的物品数据分布在不同分片
 * 
 * @author lx
 * @date 2025/06/08
 */
@Entity
@Table(name = "game_item", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_item_type", columnList = "item_type"),
    @Index(name = "idx_sharding_key", columnList = "sharding_key")
})
public class ItemEntity extends ShardingEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 物品所属用户ID
     * 关联用户表的主键，标识物品的拥有者
     * 用作分片键，确保同一用户的物品存储在同一分片
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 物品类型分类
     * 用于物品的大类区分，如：装备、道具、材料、消耗品等
     * 便于按类型查询和管理物品
     */
    @Column(name = "item_type", length = 50, nullable = false)
    private String itemType;

    /**
     * 物品配置ID
     * 对应游戏配置表中的物品ID，标识具体的物品种类
     * 通过此ID可以获取物品的基础属性、名称、描述等配置信息
     */
    @Column(name = "item_id", length = 100, nullable = false)
    private String itemId;

    /**
     * 物品数量
     * 表示该物品的堆叠数量，默认为1
     * 适用于可堆叠的道具、材料等物品
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    /**
     * 物品扩展属性
     * 使用JSON格式存储物品的动态属性
     * 如：装备强化等级、宝石镶嵌、耐久度等可变属性
     * 提供了极大的扩展性，无需修改表结构即可添加新属性
     */
    @Column(name = "properties", columnDefinition = "JSON")
    private String properties;

    // Getters and Setters
    
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户ID
     * 
     * 业务逻辑：
     * - 设置物品所属的用户ID
     * - 自动设置分片键为用户ID的字符串形式
     * - 确保物品数据按用户进行分片存储
     * 
     * 分片逻辑：
     * - 分片键基于用户ID，同一用户的所有物品存储在同一分片
     * - 提升按用户查询物品的性能
     * - 便于用户数据的管理和维护
     * 
     * @param userId 用户ID，不能为null
     */
    public void setUserId(Long userId) {
        this.userId = userId;
        // 自动设置分片键为用户ID，确保数据分片的一致性
        this.setShardingKey(String.valueOf(userId));
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    /**
     * 重写toString方法
     * 
     * 功能说明：
     * - 提供物品实体的字符串表示，便于调试和日志记录
     * - 包含所有关键字段信息和父类信息
     * - 格式化输出，提升可读性
     * 
     * @return String 物品实体的字符串表示
     */
    @Override
    public String toString() {
        return "ItemEntity{" +
                "userId=" + userId +
                ", itemType='" + itemType + '\'' +
                ", itemId='" + itemId + '\'' +
                ", quantity=" + quantity +
                ", properties='" + properties + '\'' +
                ", " + super.toString() +
                '}';
    }
}