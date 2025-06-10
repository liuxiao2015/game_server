package com.game.common.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Session类
 * 
 * 功能说明：
 * - 提供核心业务功能的实现
 * - 封装相关的数据和操作方法
 * - 支持系统模块化和代码复用
 * 
 * 设计特点：
 * - 遵循面向对象设计原则
 * - 提供清晰的接口和实现分离
 * - 支持扩展和维护
 * 
 * 使用方式：
 * - 通过公共方法提供服务
 * - 支持依赖注入和配置管理
 * - 集成框架的生命周期管理
 *
 * @author lx
 * @date 2024-01-01
 */
public class Session implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("token")
    private String token;

    @JsonProperty("createTime")
    private Long createTime;

    @JsonProperty("expireTime")
    private Long expireTime;

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("clientIp")
    private String clientIp;

    @JsonProperty("userAgent")
    private String userAgent;

    @JsonProperty("status")
    private Integer status; // 0: active, 1: expired, 2: invalidated

    public Session() {
        this.createTime = System.currentTimeMillis();
        this.status = 0;
    }

    public Session(String sessionId, Long userId, String token, Long expireTime) {
        this();
        this.sessionId = sessionId;
        this.userId = userId;
        this.token = token;
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }

    public boolean isActive() {
        return status == 0 && !isExpired();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", userId=" + userId +
                ", token='" + token + '\'' +
                ", createTime=" + createTime +
                ", expireTime=" + expireTime +
                ", deviceId='" + deviceId + '\'' +
                ", clientIp='" + clientIp + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", status=" + status +
                '}';
    }
}