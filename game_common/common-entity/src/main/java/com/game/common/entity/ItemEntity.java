package com.game.common.entity;

import com.game.frame.data.entity.ShardingEntity;
import jakarta.persistence.*;

/**
 * 物品实体
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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_type", length = 50, nullable = false)
    private String itemType;

    @Column(name = "item_id", length = 100, nullable = false)
    private String itemId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "properties", columnDefinition = "JSON")
    private String properties;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
        // 自动设置分片键为用户ID
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