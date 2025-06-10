package com.game.service.match.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 游戏匹配结果模型类
 * 
 * 功能说明：
 * - 封装游戏匹配算法执行后的结果信息
 * - 包含匹配状态、参与玩家、游戏服务器等关键数据
 * - 支持多种匹配结果状态的统一处理
 * - 提供便捷的静态工厂方法创建不同类型的结果
 * 
 * 数据模型设计：
 * - matchId：匹配会话唯一标识，用于跟踪整个匹配流程
 * - status：匹配状态枚举，包含待匹配、成功、超时、取消、错误等
 * - playerIds：参与匹配的玩家ID列表，支持1v1和多人匹配
 * - gameMode：游戏模式，与匹配请求保持一致
 * - gameServerHost/Port：分配的游戏服务器地址信息
 * - matchTime：匹配完成时间，用于统计和分析
 * - estimatedWaitTime：预估等待时间，用于用户体验优化
 * - gameConfig：游戏配置参数，包含地图、规则等设置
 * - errorMessage：错误描述信息，便于问题排查
 * 
 * 状态模型：
 * - PENDING：等待匹配中，算法正在寻找合适的对手
 * - MATCHED：匹配成功，已找到合适的玩家和服务器
 * - TIMEOUT：匹配超时，超过预设时间未找到合适对手
 * - CANCELLED：用户取消，主动退出匹配队列
 * - ERROR：系统错误，匹配过程中发生异常
 * 
 * 业务场景：
 * - 匹配算法执行完成后返回结果给客户端
 * - 客户端根据结果状态进行不同的界面展示
 * - 监控系统统计匹配成功率和平均等待时间
 * - 游戏服务器接收匹配结果进行房间创建
 * 
 * 设计特点：
 * - 使用枚举类型确保状态值的类型安全
 * - 提供静态工厂方法简化对象创建
 * - 支持链式调用和建造者模式
 * - 包含便捷的状态检查方法
 * 
 * 使用示例：
 * ```java
 * // 创建匹配成功结果
 * MatchResult result = MatchResult.success("match123", Arrays.asList(1L, 2L), "ranked");
 * result.setGameServerHost("192.168.1.100");
 * result.setGameServerPort(9999);
 * 
 * // 创建等待中结果
 * MatchResult pending = MatchResult.pending(30); // 预估30秒
 * 
 * // 检查结果状态
 * if (result.isMatched()) {
 *     // 处理匹配成功逻辑
 * }
 * ```
 * 
 * 扩展性考虑：
 * - gameConfig支持动态扩展游戏配置项
 * - 状态枚举可以添加新的匹配状态
 * - 支持A/B测试的结果数据收集
 * 
 * @author lx
 * @date 2025/01/08
 */
public class MatchResult {
    
    /** 匹配会话唯一标识，用于跟踪和关联整个匹配流程 */
    private String matchId;
    
    /** 匹配状态，表示当前匹配的执行结果 */
    private MatchStatus status;
    
    /** 参与匹配的玩家ID列表，支持单人和多人匹配 */
    private List<Long> playerIds;
    
    /** 游戏模式，与原始匹配请求保持一致 */
    private String gameMode;
    
    /** 分配的游戏服务器主机地址 */
    private String gameServerHost;
    
    /** 分配的游戏服务器端口号 */
    private Integer gameServerPort;
    
    /** 匹配结果生成时间，用于统计分析和排序 */
    private LocalDateTime matchTime;
    
    /** 预估等待时间（秒），用于提升用户体验 */
    private int estimatedWaitTime;
    
    /** 游戏配置参数，包含地图选择、游戏规则等设置 */
    private Map<String, Object> gameConfig;
    
    /** 错误描述信息，仅在状态为ERROR时有效 */
    private String errorMessage;

    /**
     * 匹配状态枚举
     * 
     * 定义匹配过程中的所有可能状态：
     * - PENDING：等待匹配，算法正在执行
     * - MATCHED：匹配成功，已找到合适的玩家组合
     * - TIMEOUT：匹配超时，未在指定时间内找到对手
     * - CANCELLED：用户取消，主动退出匹配队列
     * - ERROR：系统错误，匹配过程中发生异常
     */
    public enum MatchStatus {
        /** 等待匹配中，算法正在寻找合适的对手 */
        PENDING,
        
        /** 匹配成功，已找到合适的玩家和游戏服务器 */
        MATCHED,
        
        /** 匹配超时，超过预设时间未找到合适对手 */
        TIMEOUT,
        
        /** 用户取消匹配，主动退出匹配队列 */
        CANCELLED,
        
        /** 系统错误，匹配过程中发生异常 */
        ERROR
    }

    public MatchResult() {
        this.matchTime = LocalDateTime.now();
    }

    public MatchResult(MatchStatus status) {
        this();
        this.status = status;
    }

    public MatchResult(String matchId, List<Long> playerIds, String gameMode) {
        this();
        this.matchId = matchId;
        this.status = MatchStatus.MATCHED;
        this.playerIds = playerIds;
        this.gameMode = gameMode;
    }

    /**
     * Create successful match result
     */
    public static MatchResult success(String matchId, List<Long> playerIds, String gameMode) {
        return new MatchResult(matchId, playerIds, gameMode);
    }

    /**
     * Create pending match result
     */
    public static MatchResult pending(int estimatedWaitTime) {
        MatchResult result = new MatchResult(MatchStatus.PENDING);
        result.setEstimatedWaitTime(estimatedWaitTime);
        return result;
    }

    /**
     * Create timeout match result
     */
    public static MatchResult timeout() {
        return new MatchResult(MatchStatus.TIMEOUT);
    }

    /**
     * Create cancelled match result
     */
    public static MatchResult cancelled() {
        return new MatchResult(MatchStatus.CANCELLED);
    }

    /**
     * Create error match result
     */
    public static MatchResult error(String errorMessage) {
        MatchResult result = new MatchResult(MatchStatus.ERROR);
        result.setErrorMessage(errorMessage);
        return result;
    }

    /**
     * Check if match is successful
     */
    public boolean isMatched() {
        return status == MatchStatus.MATCHED;
    }

    /**
     * Check if match is pending
     */
    public boolean isPending() {
        return status == MatchStatus.PENDING;
    }

    /**
     * Get player count
     */
    public int getPlayerCount() {
        return playerIds != null ? playerIds.size() : 0;
    }

    // Getters and Setters
    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public List<Long> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<Long> playerIds) {
        this.playerIds = playerIds;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getGameServerHost() {
        return gameServerHost;
    }

    public void setGameServerHost(String gameServerHost) {
        this.gameServerHost = gameServerHost;
    }

    public Integer getGameServerPort() {
        return gameServerPort;
    }

    public void setGameServerPort(Integer gameServerPort) {
        this.gameServerPort = gameServerPort;
    }

    public LocalDateTime getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(LocalDateTime matchTime) {
        this.matchTime = matchTime;
    }

    public int getEstimatedWaitTime() {
        return estimatedWaitTime;
    }

    public void setEstimatedWaitTime(int estimatedWaitTime) {
        this.estimatedWaitTime = estimatedWaitTime;
    }

    public Map<String, Object> getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(Map<String, Object> gameConfig) {
        this.gameConfig = gameConfig;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "matchId='" + matchId + '\'' +
                ", status=" + status +
                ", playerCount=" + getPlayerCount() +
                ", gameMode='" + gameMode + '\'' +
                ", matchTime=" + matchTime +
                ", estimatedWaitTime=" + estimatedWaitTime +
                '}';
    }
}