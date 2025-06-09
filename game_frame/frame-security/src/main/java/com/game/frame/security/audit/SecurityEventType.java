package com.game.frame.security.audit;

/**
 * 安全事件类型枚举
 * @author lx
 * @date 2025/06/08
 */
public enum SecurityEventType {
    
    // 认证相关事件
    LOGIN_SUCCESS("用户登录成功"),
    LOGIN_FAILURE("用户登录失败"),
    LOGOUT("用户登出"),
    PASSWORD_CHANGE("密码修改"),
    MFA_SUCCESS("多因素认证成功"),
    MFA_FAILURE("多因素认证失败"),
    ACCOUNT_LOCKED("账户锁定"),
    ACCOUNT_UNLOCKED("账户解锁"),
    
    // 授权相关事件
    PERMISSION_DENIED("权限拒绝"),
    ROLE_CHANGE("角色变更"),
    PERMISSION_CHANGE("权限变更"),
    UNAUTHORIZED_ACCESS("未授权访问"),
    
    // 安全攻击事件
    SECURITY_ATTACK("安全攻击"),
    SQL_INJECTION_ATTEMPT("SQL注入尝试"),
    XSS_ATTEMPT("XSS攻击尝试"),
    CSRF_ATTEMPT("CSRF攻击尝试"),
    BRUTE_FORCE_ATTACK("暴力破解攻击"),
    DDOS_ATTACK("DDoS攻击"),
    
    // 异常行为事件
    ABNORMAL_ACCESS("异常访问"),
    UNUSUAL_LOCATION("异常登录地点"),
    MULTIPLE_SESSIONS("多重会话"),
    SUSPICIOUS_ACTIVITY("可疑活动"),
    
    // 数据安全事件
    DATA_LEAKAGE_RISK("数据泄露风险"),
    SENSITIVE_DATA_ACCESS("敏感数据访问"),
    DATA_EXPORT("数据导出"),
    DATA_MODIFICATION("数据修改"),
    
    // Token相关事件
    TOKEN_ANOMALY("Token异常"),
    TOKEN_EXPIRED("Token过期"),
    TOKEN_REVOKED("Token撤销"),
    INVALID_TOKEN("无效Token"),
    
    // 配置安全事件
    CONFIG_CHANGE("配置变更"),
    SECURITY_CONFIG_CHANGE("安全配置变更"),
    SYSTEM_CONFIG_CHANGE("系统配置变更"),
    
    // 审计相关事件
    AUDIT_LOG_ANOMALY("审计日志异常"),
    LOG_TAMPERING("日志篡改"),
    
    // 系统安全事件
    SYSTEM_INTRUSION("系统入侵"),
    MALWARE_DETECTED("恶意软件检测"),
    VULNERABILITY_EXPLOITED("漏洞利用"),
    
    // 游戏安全事件
    GAME_CHEAT_DETECTED("游戏作弊检测"),
    GAME_BOT_DETECTED("游戏机器人检测"),
    GAME_EXPLOIT("游戏漏洞利用"),
    ECONOMY_ANOMALY("经济异常"),
    
    // 自定义事件
    CUSTOM("自定义事件");
    
    private final String description;
    
    SecurityEventType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isHighSeverity() {
        return this == SECURITY_ATTACK ||
               this == SQL_INJECTION_ATTEMPT ||
               this == XSS_ATTEMPT ||
               this == BRUTE_FORCE_ATTACK ||
               this == DDOS_ATTACK ||
               this == DATA_LEAKAGE_RISK ||
               this == SYSTEM_INTRUSION ||
               this == MALWARE_DETECTED ||
               this == VULNERABILITY_EXPLOITED;
    }
    
    public boolean isCritical() {
        return this == DATA_LEAKAGE_RISK ||
               this == SYSTEM_INTRUSION ||
               this == MALWARE_DETECTED ||
               this == VULNERABILITY_EXPLOITED ||
               this == LOG_TAMPERING;
    }
    
    public boolean requiresImmediateAction() {
        return isCritical() ||
               this == DDOS_ATTACK ||
               this == BRUTE_FORCE_ATTACK ||
               this == MULTIPLE_SESSIONS;
    }
}