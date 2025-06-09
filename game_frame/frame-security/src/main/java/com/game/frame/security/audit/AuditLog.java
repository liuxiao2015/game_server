package com.game.frame.security.audit;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计日志实体
 * @author lx
 * @date 2025/06/08
 */
public class AuditLog {
    private String id;
    private String userId;
    private String username;
    private String action;          // 操作类型
    private String resource;        // 资源标识
    private String method;          // HTTP方法
    private String path;            // 请求路径
    private String ip;              // 客户端IP
    private String userAgent;       // 用户代理
    private LocalDateTime timestamp; // 操作时间
    private String status;          // 操作状态 (SUCCESS, FAILED, ERROR)
    private String errorMessage;    // 错误信息
    private Map<String, Object> details; // 详细信息
    private Long duration;          // 执行时长(ms)

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
        this.status = "SUCCESS";
    }

    public AuditLog(String userId, String action, String resource) {
        this();
        this.userId = userId;
        this.action = action;
        this.resource = resource;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                ", resource='" + resource + '\'' +
                ", ip='" + ip + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}