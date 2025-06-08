package com.game.common.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User login response
 * Contains user ID, token and user basic information
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