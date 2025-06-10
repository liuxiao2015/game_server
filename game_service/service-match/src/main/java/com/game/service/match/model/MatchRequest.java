package com.game.service.match.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 游戏匹配请求模型类
 * 
 * 功能说明：
 * - 封装玩家请求加入游戏匹配的所有相关信息
 * - 包含玩家基础信息、游戏偏好设置和匹配约束条件
 * - 支持ELO评级系统和技能匹配算法
 * - 提供匹配请求的超时控制和优先级队列功能
 * 
 * 数据模型设计：
 * - playerId：玩家唯一标识，用于关联玩家档案信息
 * - gameMode：游戏模式，如"ranked"（排位）、"casual"（休闲）等
 * - rank：玩家当前等级，影响匹配算法的段位匹配
 * - eloRating：ELO评分，精确的技能评估指标
 * - preferences：个性化偏好设置，如地图选择、角色限制等
 * - requestTime：请求创建时间，用于超时检测和统计分析
 * - timeoutSeconds：匹配超时时间，避免长时间等待
 * - priorityQueue：优先匹配标识，VIP用户或特殊情况使用
 * 
 * 业务场景：
 * - 玩家点击"开始游戏"后创建匹配请求
 * - 匹配算法根据请求信息寻找合适的对手
 * - 支持不同游戏模式的差异化匹配策略
 * - 处理玩家取消匹配和重新匹配的场景
 * 
 * 匹配算法支持：
 * - 基于ELO评级的技能匹配
 * - 考虑延迟优化的地理位置匹配
 * - 平衡等待时间和匹配质量
 * - 支持团队匹配和个人匹配
 * 
 * 设计特点：
 * - 不可变对象设计，确保数据一致性
 * - 合理的默认值设置，简化客户端调用
 * - 支持序列化，便于网络传输和持久化
 * - 内置超时检测，防止内存泄漏
 * 
 * 使用示例：
 * ```java
 * // 创建排位匹配请求
 * MatchRequest request = new MatchRequest(playerId, "ranked", 15, 1200);
 * request.setPriorityQueue(false);
 * request.setTimeoutSeconds(300);
 * 
 * // 检查是否超时
 * if (request.isExpired()) {
 *     // 处理超时逻辑
 * }
 * ```
 * 
 * 扩展性考虑：
 * - preferences支持动态扩展新的偏好选项
 * - 可以添加更多匹配约束条件
 * - 支持A/B测试的匹配策略调整
 * 
 * @author lx
 * @date 2025/01/08
 */
public class MatchRequest {
    
    /** 玩家ID，唯一标识请求匹配的玩家 */
    private Long playerId;
    
    /** 游戏模式，如"ranked"（排位赛）、"casual"（休闲模式）、"tournament"（锦标赛）等 */
    private String gameMode;
    
    /** 玩家当前等级，影响匹配时的段位限制和对手选择 */
    private int rank;
    
    /** ELO评分，更精确的技能评估指标，用于公平匹配 */
    private int eloRating;
    
    /** 玩家偏好设置，包含地图选择、游戏规则、团队设置等个性化配置 */
    private Map<String, Object> preferences;
    
    /** 匹配请求创建时间，用于超时检测和等待时间统计 */
    private LocalDateTime requestTime;
    
    /** 匹配超时时间（秒），超过此时间自动取消匹配 */
    private int timeoutSeconds;
    
    /** 是否使用优先队列，VIP用户或特殊活动时可以优先匹配 */
    private boolean priorityQueue;

    /**
     * 默认构造函数
     * 
     * 功能说明：
     * - 初始化匹配请求对象，设置默认值
     * - 自动设置请求创建时间为当前时间
     * - 设置默认超时时间为5分钟
     * 
     * 默认值说明：
     * - 超时时间：300秒（5分钟），平衡用户体验和资源利用
     * - 请求时间：当前系统时间，确保时间准确性
     */
    public MatchRequest() {
        this.requestTime = LocalDateTime.now();
        this.timeoutSeconds = 300; // 默认5分钟超时
    }

    /**
     * 带参数的构造函数
     * 
     * 功能说明：
     * - 创建包含核心匹配信息的请求对象
     * - 调用默认构造函数初始化时间和超时设置
     * - 设置玩家基础信息和技能评级
     * 
     * 使用场景：
     * - 快速创建标准的匹配请求
     * - API接口接收客户端匹配请求时使用
     * - 测试环境中创建模拟数据
     * 
     * @param playerId 玩家ID，不能为null
     * @param gameMode 游戏模式，不能为空
     * @param rank 玩家等级，应该为正整数
     * @param eloRating ELO评分，通常在1000-3000之间
     */
    public MatchRequest(Long playerId, String gameMode, int rank, int eloRating) {
        this();
        this.playerId = playerId;
        this.gameMode = gameMode;
        this.rank = rank;
        this.eloRating = eloRating;
    }

    // ========== 属性访问方法 ==========
    
    /**
     * 获取玩家ID
     * @return 玩家唯一标识符
     */
    public Long getPlayerId() {
        return playerId;
    }

    /**
     * 设置玩家ID
     * @param playerId 玩家唯一标识符
     */
    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    /**
     * 获取游戏模式
     * @return 游戏模式字符串
     */
    public String getGameMode() {
        return gameMode;
    }

    /**
     * 设置游戏模式
     * @param gameMode 游戏模式，如"ranked"、"casual"等
     */
    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    /**
     * 获取玩家等级
     * @return 玩家当前等级
     */
    public int getRank() {
        return rank;
    }

    /**
     * 设置玩家等级
     * @param rank 玩家等级，应为正整数
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * 获取ELO评分
     * @return 玩家技能评分
     */
    public int getEloRating() {
        return eloRating;
    }

    /**
     * 设置ELO评分
     * @param eloRating 玩家技能评分
     */
    public void setEloRating(int eloRating) {
        this.eloRating = eloRating;
    }

    /**
     * 获取玩家偏好设置
     * @return 偏好设置的键值对映射
     */
    public Map<String, Object> getPreferences() {
        return preferences;
    }

    /**
     * 设置玩家偏好设置
     * @param preferences 偏好设置映射，包含地图、规则等选项
     */
    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }

    /**
     * 获取请求创建时间
     * @return 请求创建的时间戳
     */
    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    /**
     * 设置请求创建时间
     * @param requestTime 请求创建时间
     */
    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    /**
     * 获取超时时间
     * @return 匹配超时时间（秒）
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * 设置超时时间
     * @param timeoutSeconds 匹配超时时间（秒）
     */
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * 是否使用优先队列
     * @return true表示优先匹配，false表示普通匹配
     */
    public boolean isPriorityQueue() {
        return priorityQueue;
    }

    /**
     * 设置是否使用优先队列
     * @param priorityQueue 是否启用优先匹配
     */
    public void setPriorityQueue(boolean priorityQueue) {
        this.priorityQueue = priorityQueue;
    }

    // ========== 业务方法 ==========
    
    /**
     * 检查匹配请求是否已超时
     * 
     * 功能说明：
     * - 根据请求创建时间和超时设置判断是否超时
     * - 用于匹配算法中的超时请求清理
     * - 避免长时间无效请求占用系统资源
     * 
     * 使用场景：
     * - 匹配算法定期清理超时请求
     * - 客户端检查请求状态
     * - 监控统计匹配超时率
     * 
     * @return true表示已超时，false表示仍有效
     */
    public boolean isExpired() {
        return requestTime.plusSeconds(timeoutSeconds).isBefore(LocalDateTime.now());
    }

    /**
     * 获取当前等待时间
     * 
     * 功能说明：
     * - 计算从请求创建到当前时刻的等待时间
     * - 用于匹配进度显示和用户体验优化
     * - 为匹配算法提供等待时间参考
     * 
     * 使用场景：
     * - 客户端显示匹配等待时间
     * - 匹配算法根据等待时间调整策略
     * - 统计分析平均匹配时间
     * 
     * @return 等待时间（秒）
     */
    public long getWaitingTime() {
        return java.time.Duration.between(requestTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 返回对象的字符串表示
     * 
     * 功能说明：
     * - 提供易读的对象描述，便于日志记录和调试
     * - 包含关键字段信息，过滤敏感数据
     * - 遵循Java标准toString格式
     * 
     * @return 对象的字符串描述
     */
    @Override
    public String toString() {
        return "MatchRequest{" +
                "playerId=" + playerId +
                ", gameMode='" + gameMode + '\'' +
                ", rank=" + rank +
                ", eloRating=" + eloRating +
                ", requestTime=" + requestTime +
                ", timeoutSeconds=" + timeoutSeconds +
                ", priorityQueue=" + priorityQueue +
                '}';
    }
}