package com.game.common.entity;

import com.game.frame.data.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 游戏用户实体类
 * 
 * 功能说明：
 * - 表示游戏系统中的用户账户信息和游戏数据
 * - 集成JPA持久化框架，支持数据库操作和ORM映射
 * - 继承BaseEntity，包含通用的实体字段和审计功能
 * - 提供用户基础信息、游戏进度、VIP状态等核心数据
 * 
 * 数据库设计：
 * - 表名：game_user
 * - 主键：继承自BaseEntity的id字段
 * - 索引：username唯一索引、last_login_time时间索引
 * - 约束：username字段唯一约束，关键字段非空约束
 * 
 * 实体字段说明：
 * - username：用户登录名，唯一标识，用于登录验证
 * - nickname：用户昵称，可重复，用于游戏内显示
 * - level：用户等级，影响游戏功能解锁和能力值
 * - exp：经验值，用于等级提升和成长系统
 * - vipLevel：VIP等级，影响特权功能和付费服务
 * - lastLoginTime：最后登录时间，用于活跃度统计和离线奖励
 * 
 * 业务规则：
 * - 用户名不可重复，长度限制在50字符以内
 * - 昵称可重复，长度限制在100字符以内
 * - 等级从1开始，经验值和VIP等级从0开始
 * - 登录时间在用户每次登录时自动更新
 * 
 * 索引策略：
 * - username索引：支持快速的登录验证查询
 * - last_login_time索引：支持活跃用户统计和离线用户查询
 * 
 * 扩展性考虑：
 * - 继承BaseEntity提供创建时间、更新时间、版本号等审计字段
 * - 可以通过关联表扩展更多的用户属性和游戏数据
 * - 支持分库分表的用户数据分片策略
 * 
 * 使用场景：
 * - 用户注册和登录验证
 * - 游戏内用户信息展示
 * - 用户等级和经验系统
 * - VIP特权和付费服务
 * - 用户活跃度统计和分析
 * 
 * 性能优化：
 * - 通过索引优化常用查询路径
 * - 使用合适的字段类型减少存储空间
 * - 支持读写分离和缓存策略
 * 
 * 数据一致性：
 * - 继承BaseEntity的乐观锁机制
 * - 通过数据库约束保证数据完整性
 * - 支持事务操作确保操作原子性
 *
 * @author lx
 * @date 2025/06/08
 */
@Entity
@Table(name = "game_user", indexes = {
    @Index(name = "idx_username", columnList = "username"),      // 用户名索引，支持快速登录验证
    @Index(name = "idx_last_login", columnList = "last_login_time") // 登录时间索引，支持活跃度统计
})
public class UserEntity extends BaseEntity {

    /** 序列化版本号，用于对象序列化和版本兼容性控制 */
    private static final long serialVersionUID = 1L;

    /** 
     * 用户登录名
     * 
     * 业务规则：
     * - 全局唯一，不可重复
     * - 长度限制：1-50个字符
     * - 用于用户登录验证和身份识别
     * - 创建后不建议修改，影响关联数据
     */
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    /** 
     * 用户昵称
     * 
     * 业务规则：
     * - 可重复，允许多个用户使用相同昵称
     * - 长度限制：1-100个字符
     * - 用于游戏内显示和社交功能
     * - 支持用户自定义修改
     */
    @Column(name = "nickname", length = 100)
    private String nickname;

    /** 
     * 用户等级
     * 
     * 业务规则：
     * - 默认值：1（新用户初始等级）
     * - 取值范围：1-999（根据游戏设计确定上限）
     * - 影响功能解锁、能力值、权限等
     * - 通过经验值累积提升
     */
    @Column(name = "level", nullable = false)
    private Integer level = 1;

    /** 
     * 用户经验值
     * 
     * 业务规则：
     * - 默认值：0（新用户无经验）
     * - 通过游戏活动获得：任务、战斗、活动等
     * - 累积到一定数值时可提升等级
     * - 支持经验值的消耗和转换
     */
    @Column(name = "exp", nullable = false)
    private Long exp = 0L;

    /** 
     * VIP等级
     * 
     * 业务规则：
     * - 默认值：0（普通用户）
     * - 取值范围：0-15（根据VIP体系设计）
     * - 通过充值或特殊活动提升
     * - 享受对应等级的特权和服务
     */
    @Column(name = "vip_level", nullable = false)
    private Integer vipLevel = 0;

    /** 
     * 最后登录时间
     * 
     * 业务规则：
     * - 每次用户登录时自动更新
     * - 用于计算离线时长和离线奖励
     * - 支持用户活跃度统计和分析
     * - 新用户首次登录时设置
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    // ========== 属性访问方法 ==========
    
    /**
     * 获取用户登录名
     * @return 用户唯一登录标识
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户登录名
     * @param username 用户登录名，需保证全局唯一性
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取用户昵称
     * @return 用户显示昵称
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 设置用户昵称
     * @param nickname 用户昵称，用于游戏内显示
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 获取用户等级
     * @return 当前用户等级
     */
    public Integer getLevel() {
        return level;
    }

    /**
     * 设置用户等级
     * @param level 新的用户等级，应配合经验值一起更新
     */
    public void setLevel(Integer level) {
        this.level = level;
    }

    /**
     * 获取用户经验值
     * @return 当前累积经验值
     */
    public Long getExp() {
        return exp;
    }

    /**
     * 设置用户经验值
     * @param exp 新的经验值，可能触发等级提升
     */
    public void setExp(Long exp) {
        this.exp = exp;
    }

    /**
     * 获取VIP等级
     * @return 当前VIP等级
     */
    public Integer getVipLevel() {
        return vipLevel;
    }

    /**
     * 设置VIP等级
     * @param vipLevel 新的VIP等级，影响特权和服务
     */
    public void setVipLevel(Integer vipLevel) {
        this.vipLevel = vipLevel;
    }

    /**
     * 获取最后登录时间
     * @return 用户最后一次登录的时间
     */
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * 设置最后登录时间
     * @param lastLoginTime 最新的登录时间，通常在登录时自动更新
     */
    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    /**
     * 返回实体对象的字符串表示
     * 
     * 功能说明：
     * - 包含所有关键字段的信息，便于调试和日志记录
     * - 结合父类BaseEntity的toString方法，提供完整信息
     * - 不包含敏感信息，可安全用于日志输出
     * 
     * @return 实体对象的详细字符串描述
     */
    @Override
    public String toString() {
        return "UserEntity{" +
                "username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", level=" + level +
                ", exp=" + exp +
                ", vipLevel=" + vipLevel +
                ", lastLoginTime=" + lastLoginTime +
                ", " + super.toString() +
                '}';
    }
}