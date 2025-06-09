package com.game.frame.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * 分片实体基类
 * @author lx
 * @date 2025/06/08
 */
@MappedSuperclass
public abstract class ShardingEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "sharding_key", nullable = false)
    private String shardingKey; // 分片键

    public String getShardingKey() {
        return shardingKey;
    }

    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{shardingKey=" + shardingKey + 
               ", " + super.toString() + "}";
    }
}