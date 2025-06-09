package com.game.common.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * UserLogin响应对象
 * 
 * 功能说明：
 * - 封装网络通信的数据传输对象
 * - 提供数据序列化和反序列化支持
 * - 实现参数验证和格式校验
 * - 支持JSON和其他格式的数据转换
 * 
 * 数据结构：
 * - 包含业务处理所需的核心字段
 * - 支持可选字段和默认值设置
 * - 提供数据完整性验证机制
 * 
 * 使用场景：
 * - 客户端与服务器的数据交互
 * - 微服务间的接口调用
 * - API接口的参数传递
 *
 * @author lx
 * @date 2024-01-01
 */
public class UserLoginResponse extends BaseResponse<UserLoginResponse.LoginData> {

    private static final long serialVersionUID = 1L;

    public UserLoginResponse() {
        super();
    }

    public UserLoginResponse(LoginData data) {
        super(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static UserLoginResponse success(Long userId, String token, String nickname, Integer level) {
        LoginData data = new LoginData(userId, token, nickname, level);
        return new UserLoginResponse(data);
    }

    public static UserLoginResponse error(String message) {
        UserLoginResponse response = new UserLoginResponse();
        response.setCode(ERROR_CODE);
        response.setMessage(message);
        return response;
    }

    /**
     * Login response data
     */
    public static class LoginData {

        @JsonProperty("userId")
        private Long userId;

        @JsonProperty("token")
        private String token;

        @JsonProperty("nickname")
        private String nickname;

        @JsonProperty("level")
        private Integer level;

        @JsonProperty("lastLoginTime")
        private Long lastLoginTime;

        @JsonProperty("isFirstLogin")
        private Boolean isFirstLogin;

        public LoginData() {
        }

        public LoginData(Long userId, String token, String nickname, Integer level) {
            this.userId = userId;
            this.token = token;
            this.nickname = nickname;
            this.level = level;
            this.lastLoginTime = System.currentTimeMillis();
            this.isFirstLogin = false;
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

        public Long getLastLoginTime() {
            return lastLoginTime;
        }

        public void setLastLoginTime(Long lastLoginTime) {
            this.lastLoginTime = lastLoginTime;
        }

        public Boolean getIsFirstLogin() {
            return isFirstLogin;
        }

        public void setIsFirstLogin(Boolean isFirstLogin) {
            this.isFirstLogin = isFirstLogin;
        }

        @Override
        public String toString() {
            return "LoginData{" +
                    "userId=" + userId +
                    ", token='" + token + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", level=" + level +
                    ", lastLoginTime=" + lastLoginTime +
                    ", isFirstLogin=" + isFirstLogin +
                    '}';
        }
    }
}