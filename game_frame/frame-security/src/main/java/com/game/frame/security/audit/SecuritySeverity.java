package com.game.frame.security.audit;

/**
 * 安全事件严重程度枚举
 * @author lx
 * @date 2025/06/08
 */
public enum SecuritySeverity {
    
    /**
     * 信息级别 - 正常的安全事件，如成功登录
     */
    INFO(1, "信息", "Information"),
    
    /**
     * 调试级别 - 调试信息，通常不需要特别关注
     */
    DEBUG(2, "调试", "Debug"),
    
    /**
     * 注意级别 - 需要注意但不紧急的事件
     */
    NOTICE(3, "注意", "Notice"),
    
    /**
     * 警告级别 - 潜在的安全问题，需要关注
     */
    WARNING(4, "警告", "Warning"),
    
    /**
     * 中等级别 - 中等严重程度的安全事件
     */
    MEDIUM(5, "中等", "Medium"),
    
    /**
     * 高级别 - 严重的安全事件，需要立即处理
     */
    HIGH(6, "高", "High"),
    
    /**
     * 严重级别 - 非常严重的安全事件，可能导致系统损害
     */
    CRITICAL(7, "严重", "Critical"),
    
    /**
     * 紧急级别 - 最高级别的安全事件，需要立即响应
     */
    EMERGENCY(8, "紧急", "Emergency");
    
    private final int level;
    private final String chineseName;
    private final String englishName;
    
    SecuritySeverity(int level, String chineseName, String englishName) {
        this.level = level;
        this.chineseName = chineseName;
        this.englishName = englishName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getChineseName() {
        return chineseName;
    }
    
    public String getEnglishName() {
        return englishName;
    }
    
    /**
     * 是否需要立即通知
     */
    public boolean requiresImmediateNotification() {
        return level >= HIGH.level;
    }
    
    /**
     * 是否需要立即响应
     */
    public boolean requiresImmediateResponse() {
        return level >= CRITICAL.level;
    }
    
    /**
     * 是否需要升级处理
     */
    public boolean requiresEscalation() {
        return this == EMERGENCY || this == CRITICAL;
    }
    
    /**
     * 获取颜色代码（用于UI显示）
     */
    public String getColorCode() {
        switch (this) {
            case DEBUG:
            case INFO:
                return "#28a745"; // 绿色
            case NOTICE:
                return "#17a2b8"; // 蓝色
            case WARNING:
                return "#ffc107"; // 黄色
            case MEDIUM:
                return "#fd7e14"; // 橙色
            case HIGH:
                return "#dc3545"; // 红色
            case CRITICAL:
                return "#6f42c1"; // 紫色
            case EMERGENCY:
                return "#000000"; // 黑色
            default:
                return "#6c757d"; // 灰色
        }
    }
    
    /**
     * 根据级别获取严重程度
     */
    public static SecuritySeverity fromLevel(int level) {
        for (SecuritySeverity severity : values()) {
            if (severity.level == level) {
                return severity;
            }
        }
        return INFO; // 默认返回INFO级别
    }
    
    /**
     * 比较严重程度
     */
    public boolean isMoreSevereThan(SecuritySeverity other) {
        return this.level > other.level;
    }
    
    /**
     * 比较严重程度
     */
    public boolean isLessSevereThan(SecuritySeverity other) {
        return this.level < other.level;
    }
    
    /**
     * 获取响应时间要求（分钟）
     */
    public int getResponseTimeMinutes() {
        switch (this) {
            case EMERGENCY:
                return 5; // 5分钟内响应
            case CRITICAL:
                return 15; // 15分钟内响应
            case HIGH:
                return 60; // 1小时内响应
            case MEDIUM:
                return 240; // 4小时内响应
            case WARNING:
                return 1440; // 24小时内响应
            default:
                return -1; // 无时间要求
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - Level %d", englishName, chineseName, level);
    }
}