package com.game.common.entity;

import com.game.frame.data.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 用户实体
 * @author lx
 * @date 2025/06/08
 */
@Entity
@Table(name = "game_user", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_last_login", columnList = "last_login_time")
})
public class UserEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Column(name = "exp", nullable = false)
    private Long exp = 0L;

    @Column(name = "vip_level", nullable = false)
    private Integer vipLevel = 0;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public Integer getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(Integer vipLevel) {
        this.vipLevel = vipLevel;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

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