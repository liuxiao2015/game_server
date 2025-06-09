package com.game.frame.security.auth;

/**
 * 认证用户信息
 * @author lx
 * @date 2025/06/08
 */
public class AuthUser {
    private Long userId;
    private String username;
    private String sessionId;
    private String[] permissions;
    private String[] roles;
    private Long loginTime;
    private String loginIp;

    public AuthUser() {}

    public AuthUser(Long userId, String username, String sessionId) {
        this.userId = userId;
        this.username = username;
        this.sessionId = sessionId;
        this.loginTime = System.currentTimeMillis();
    }

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String[] getPermissions() { return permissions; }
    public void setPermissions(String[] permissions) { this.permissions = permissions; }
    
    public String[] getRoles() { return roles; }
    public void setRoles(String[] roles) { this.roles = roles; }
    
    public Long getLoginTime() { return loginTime; }
    public void setLoginTime(Long loginTime) { this.loginTime = loginTime; }
    
    public String getLoginIp() { return loginIp; }
    public void setLoginIp(String loginIp) { this.loginIp = loginIp; }
    
    @Override
    public String toString() {
        return "AuthUser{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", loginTime=" + loginTime +
                ", loginIp='" + loginIp + '\'' +
                '}';
    }
}