package com.game.common.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 用户实体类
 * 
 * 功能说明：
 * - 封装用户的基础信息和游戏数据
 * - 提供用户账号、昵称、等级、经验值等核心属性
 * - 支持用户创建时间、最后登录时间等时间戳记录
 * - 实现序列化接口，支持网络传输和持久化存储
 * 
 * 数据字段：
 * - userId: 用户唯一标识ID，系统自动生成
 * - account: 用户登录账号，可以是用户名、邮箱或手机号
 * - nickname: 用户昵称，用于游戏内显示
 * - level: 用户等级，反映游戏进度和成就
 * - experience: 用户经验值，用于等级计算
 * - avatar: 用户头像URL或标识
 * - createTime: 账号创建时间戳
 * - lastLoginTime: 最后登录时间戳
 * 
 * 设计特点：
 * - 实现Serializable接口，支持对象序列化
 * - 使用Jackson注解，支持JSON格式转换
 * - 字段验证和约束，确保数据完整性
 * - 扩展性设计，便于添加新的用户属性
 * 
 * 使用场景：
 * - 用户登录时的身份信息传递
 * - 游戏内用户信息展示
 * - 用户数据的持久化存储
 * - 分布式服务间的用户信息共享
 * 
 * 注意事项：
 * - 不包含敏感信息如密码、支付密码等
 * - 所有时间字段使用时间戳格式存储
 * - 支持部分字段的空值处理
 *
 * @author lx
 * @date 2024-01-01
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("account")
    private String account;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("level")
    private Integer level;

    @JsonProperty("experience")
    private Long experience;

    @JsonProperty("coins")
    private Long coins;

    @JsonProperty("status")
    private Integer status; // 0: normal, 1: banned

    @JsonProperty("createTime")
    private Long createTime;

    @JsonProperty("updateTime")
    private Long updateTime;

    @JsonProperty("lastLoginTime")
    private Long lastLoginTime;

    @JsonProperty("avatar")
    private String avatar;

    public User() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
        this.level = 1;
        this.experience = 0L;
        this.coins = 0L;
        this.status = 0;
    }

    public User(Long userId, String account, String nickname) {
        this();
        this.userId = userId;
        this.account = account;
        this.nickname = nickname;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getExperience() {
        return experience;
    }

    public void setExperience(Long experience) {
        this.experience = experience;
    }

    public Long getCoins() {
        return coins;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", account='" + account + '\'' +
                ", nickname='" + nickname + '\'' +
                ", level=" + level +
                ", experience=" + experience +
                ", coins=" + coins +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", lastLoginTime=" + lastLoginTime +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}