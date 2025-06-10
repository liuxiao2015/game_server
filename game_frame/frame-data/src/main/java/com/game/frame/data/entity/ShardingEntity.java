package com.game.frame.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * 分片实体基类
 * 
 * 功能说明：
 * - 为需要分库分表的实体提供分片键支持
 * - 继承BaseEntity获得完整的基础实体功能
 * - 支持水平扩展和大数据量的分布式存储
 * - 提供统一的分片策略和路由机制
 * 
 * 设计思路：
 * - 扩展BaseEntity，添加分片键字段
 * - 使用@MappedSuperclass注解，不创建独立表
 * - 分片键通常基于用户ID或业务ID设计
 * - 支持多种分片策略的灵活实现
 * 
 * 分片策略：
 * - 用户分片：基于用户ID进行分片，同一用户数据集中存储
 * - 时间分片：基于时间字段进行分片，便于历史数据管理
 * - 哈希分片：基于业务字段哈希值分片，均匀分布数据
 * - 范围分片：基于数值范围分片，便于范围查询
 * 
 * 使用场景：
 * - 大用户量场景：用户数据按用户ID分片存储
 * - 高并发写入：分散写入压力到不同数据库
 * - 海量数据存储：超出单库容量限制的数据
 * - 跨地域部署：不同地区的数据分片存储
 * 
 * 技术特点：
 * - 透明分片：应用层无需感知具体的分片逻辑
 * - 路由优化：基于分片键的高效数据路由
 * - 扩展性好：支持动态增加分片和数据迁移
 * - 查询优化：包含分片键的查询性能最优
 * 
 * 注意事项：
 * - 分片键一旦设置建议不要修改
 * - 跨分片查询性能相对较差
 * - 需要考虑数据一致性和事务处理
 * 
 * @author lx
 * @date 2025/06/08
 */
@MappedSuperclass
public abstract class ShardingEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 分片键字段
     * 
     * 功能说明：
     * - 用于确定数据存储在哪个分片的关键字段
     * - 通常基于用户ID、租户ID等业务标识生成
     * - 分片路由算法基于此字段计算目标分片
     * 
     * 设计原则：
     * - 分布均匀：确保数据在各分片间均匀分布
     * - 查询友好：大部分查询都应包含分片键
     * - 稳定不变：分片键一旦设置应保持稳定
     * - 业务相关：与主要业务场景紧密相关
     * 
     * 常见策略：
     * - 用户ID：用户相关数据使用用户ID作为分片键
     * - 时间戳：日志类数据使用时间进行分片
     * - 哈希值：复合字段的哈希值作为分片键
     * - 地域码：按地域分片的业务使用地域标识
     */
    @Column(name = "sharding_key", nullable = false)
    private String shardingKey;

    /**
     * 获取分片键
     * @return String 分片键值
     */
    public String getShardingKey() {
        return shardingKey;
    }

    /**
     * 设置分片键
     * 
     * 业务逻辑：
     * - 设置实体的分片键值
     * - 分片键决定了数据的存储位置
     * - 通常在实体创建时设置，后续不建议修改
     * 
     * 使用注意：
     * - 分片键应该在数据插入前设置
     * - 修改分片键可能导致数据路由错误
     * - 建议使用业务相关的稳定标识
     * 
     * @param shardingKey 分片键值
     */
    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }

    /**
     * 重写toString方法
     * 
     * 功能说明：
     * - 提供分片实体的字符串表示
     * - 包含分片键信息和父类信息
     * - 便于调试和日志记录
     * 
     * @return String 实体的字符串表示
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{shardingKey=" + shardingKey + 
               ", " + super.toString() + "}";
    }
}