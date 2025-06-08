package com.game.common.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * User entity
 * Contains user ID, nickname, level, creation time and other user information
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