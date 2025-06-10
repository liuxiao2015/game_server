package com.game.frame.data.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据库实体基类
 * 
 * 功能说明：
 * - 为所有数据库实体提供通用的基础字段和功能
 * - 集成JPA审计功能，自动管理创建时间和修改时间
 * - 提供乐观锁支持，防止并发更新冲突
 * - 实现逻辑删除机制，避免数据物理删除
 * 
 * 设计思路：
 * - 使用@MappedSuperclass注解，不会创建单独的数据库表
 * - 继承此类的实体会自动获得所有基础字段
 * - 集成Spring Data JPA审计功能，自动填充时间字段
 * - 提供统一的equals、hashCode和toString实现
 * 
 * 基础字段：
 * - id: 主键，使用数据库自增策略
 * - createTime: 创建时间，自动设置且不可更新
 * - updateTime: 更新时间，每次保存时自动更新
 * - version: 版本号，用于乐观锁控制
 * - deleted: 删除标记，0-未删除，1-已删除
 * 
 * 使用场景：
 * - 游戏用户实体：User、Player等用户相关实体
 * - 游戏数据实体：Equipment、Item、Quest等游戏对象
 * - 配置实体：GameConfig、ServerConfig等配置数据
 * - 日志实体：UserLog、GameLog等日志记录
 * 
 * 技术特点：
 * - JPA审计：自动维护时间戳字段，无需手动设置
 * - 乐观锁：通过version字段防止并发修改冲突
 * - 逻辑删除：通过deleted字段标记删除状态
 * - 序列化支持：实现Serializable接口，支持缓存和分布式传输
 * 
 * 乐观锁机制：
 * - 每次更新操作会检查version字段
 * - 如果version不匹配则抛出OptimisticLockException
 * - 适用于读多写少的业务场景
 * - 避免了悲观锁的性能开销
 * 
 * @author lx
 * @date 2025/06/08
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     * 使用数据库自增策略生成唯一标识符
     * 所有实体的主键统一使用Long类型，支持大数据量
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 创建时间
     * 记录实体首次创建的时间戳
     * 由JPA审计功能自动设置，updatable=false确保不可修改
     */
    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     * 记录实体最后一次修改的时间戳
     * 由JPA审计功能自动维护，每次保存时自动更新
     */
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 版本号(乐观锁)
     * 用于实现乐观锁机制，防止并发修改冲突
     * 每次更新操作会自动递增版本号
     * 如果更新时版本号不匹配，会抛出OptimisticLockException
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * 逻辑删除标记
     * 0: 正常状态，未删除
     * 1: 已删除状态，逻辑删除
     * 使用逻辑删除避免数据物理删除，便于数据恢复和审计
     */
    @Column(name = "deleted")
    private Integer deleted = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    /**
     * 重写equals方法
     * 
     * 业务逻辑：
     * - 基于ID进行相等性判断，符合JPA实体的最佳实践
     * - 如果ID为null，则认为是新实体，不相等
     * - 只有相同类型且ID相同的实体才认为相等
     * 
     * 注意事项：
     * - 确保equals和hashCode的一致性
     * - 适用于集合操作和缓存场景
     * 
     * @param obj 比较对象
     * @return boolean 是否相等
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BaseEntity that = (BaseEntity) obj;
        return id != null && id.equals(that.id);
    }

    /**
     * 重写hashCode方法
     * 
     * 业务逻辑：
     * - 基于ID计算哈希值，与equals方法保持一致
     * - ID为null时返回0，避免空指针异常
     * - 确保相等的对象具有相同的哈希值
     * 
     * @return int 哈希值
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * 重写toString方法
     * 
     * 功能说明：
     * - 提供实体对象的字符串表示，便于调试和日志记录
     * - 包含类名和所有基础字段信息
     * - 格式化输出，提升可读性
     * 
     * @return String 实体的字符串表示
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + 
               ", createTime=" + createTime + 
               ", updateTime=" + updateTime + 
               ", version=" + version + 
               ", deleted=" + deleted + "}";
    }
}