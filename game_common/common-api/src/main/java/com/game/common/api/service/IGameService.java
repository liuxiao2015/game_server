package com.game.common.api.service;

import com.game.common.model.response.Result;

/**
 * 游戏核心服务接口
 * 
 * 功能说明：
 * - 管理玩家进入和退出游戏的完整流程
 * - 提供游戏数据同步和状态管理功能
 * - 维护游戏会话的生命周期和心跳机制
 * - 支持多种游戏类型和分布式游戏架构
 * 
 * 设计思路：
 * - 分离游戏逻辑和基础框架，提供统一的游戏服务接口
 * - 支持游戏服务器的水平扩展和负载均衡
 * - 实现游戏状态的实时同步和持久化存储
 * - 提供游戏会话的超时处理和异常恢复机制
 * 
 * 使用场景：
 * - 玩家从大厅进入具体游戏场景
 * - 游戏过程中的数据保存和同步
 * - 玩家断线重连和游戏状态恢复
 * - 游戏服务器间的负载调度和迁移
 * 
 * 架构特点：
 * - 支持游戏会话的分布式管理
 * - 提供游戏服务器的服务发现机制
 * - 实现游戏数据的版本控制和冲突解决
 *
 * @author lx
 * @date 2024-01-01
 */
public interface IGameService {

    /**
     * Enter game
     *
     * @param userId user ID
     * @param gameType game type
     * @return enter game result
     */
    Result<GameEnterData> enterGame(Long userId, String gameType);

    /**
     * Exit game
     *
     * @param userId user ID
     * @return exit game result
     */
    Result<Void> exitGame(Long userId);

    /**
     * Synchronize game data
     *
     * @param userId user ID
     * @param gameData game data
     * @return sync result
     */
    Result<Void> syncGameData(Long userId, String gameData);

    /**
     * Get user game status
     *
     * @param userId user ID
     * @return game status
     */
    Result<GameStatus> getUserGameStatus(Long userId);

    /**
     * Heartbeat to keep game session alive
     *
     * @param userId user ID
     * @return heartbeat result
     */
    Result<Void> heartbeat(Long userId);

    /**
     * Game enter data
     */
    class GameEnterData {
        private String gameSessionId;
        private String gameServerHost;
        private Integer gameServerPort;
        private Long enterTime;

        public GameEnterData() {
        }

        public GameEnterData(String gameSessionId, String gameServerHost, Integer gameServerPort) {
            this.gameSessionId = gameSessionId;
            this.gameServerHost = gameServerHost;
            this.gameServerPort = gameServerPort;
            this.enterTime = System.currentTimeMillis();
        }

        // Getters and setters
        public String getGameSessionId() { return gameSessionId; }
        public void setGameSessionId(String gameSessionId) { this.gameSessionId = gameSessionId; }
        public String getGameServerHost() { return gameServerHost; }
        public void setGameServerHost(String gameServerHost) { this.gameServerHost = gameServerHost; }
        public Integer getGameServerPort() { return gameServerPort; }
        public void setGameServerPort(Integer gameServerPort) { this.gameServerPort = gameServerPort; }
        public Long getEnterTime() { return enterTime; }
        public void setEnterTime(Long enterTime) { this.enterTime = enterTime; }

        @Override
        public String toString() {
            return "GameEnterData{gameSessionId='" + gameSessionId + "', gameServerHost='" + gameServerHost + 
                   "', gameServerPort=" + gameServerPort + ", enterTime=" + enterTime + '}';
        }
    }

    /**
     * Game status
     */
    class GameStatus {
        private String gameType;
        private String status; // IDLE, IN_GAME, PAUSED
        private String gameSessionId;
        private Long enterTime;
        private Long lastActiveTime;

        public GameStatus() {
        }

        public GameStatus(String gameType, String status, String gameSessionId) {
            this.gameType = gameType;
            this.status = status;
            this.gameSessionId = gameSessionId;
            this.enterTime = System.currentTimeMillis();
            this.lastActiveTime = this.enterTime;
        }

        // Getters and setters
        public String getGameType() { return gameType; }
        public void setGameType(String gameType) { this.gameType = gameType; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getGameSessionId() { return gameSessionId; }
        public void setGameSessionId(String gameSessionId) { this.gameSessionId = gameSessionId; }
        public Long getEnterTime() { return enterTime; }
        public void setEnterTime(Long enterTime) { this.enterTime = enterTime; }
        public Long getLastActiveTime() { return lastActiveTime; }
        public void setLastActiveTime(Long lastActiveTime) { this.lastActiveTime = lastActiveTime; }

        @Override
        public String toString() {
            return "GameStatus{gameType='" + gameType + "', status='" + status + "', gameSessionId='" + 
                   gameSessionId + "', enterTime=" + enterTime + ", lastActiveTime=" + lastActiveTime + '}';
        }
    }
}