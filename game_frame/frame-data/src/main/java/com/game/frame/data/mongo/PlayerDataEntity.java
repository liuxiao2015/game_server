package com.game.frame.data.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 玩家数据实体（MongoDB）
 * @author lx
 * @date 2025/06/08
 */
@Document(collection = "player_data")
public class PlayerDataEntity {

    @Id
    private String id;

    @Field("user_id")
    private Long userId;

    @Field("player_name")
    private String playerName;

    @Field("game_data")
    private Map<String, Object> gameData;

    @Field("statistics")
    private Map<String, Object> statistics;

    @Field("create_time")
    private LocalDateTime createTime;

    @Field("update_time")
    private LocalDateTime updateTime;

    public PlayerDataEntity() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Map<String, Object> getGameData() {
        return gameData;
    }

    public void setGameData(Map<String, Object> gameData) {
        this.gameData = gameData;
    }

    public Map<String, Object> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Object> statistics) {
        this.statistics = statistics;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "PlayerDataEntity{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", playerName='" + playerName + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}