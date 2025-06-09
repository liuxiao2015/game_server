package com.game.service.match.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Match request model
 * Represents a player's request to join a match
 *
 * @author lx
 * @date 2025/01/08
 */
/**
 * MatchRequest
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
public class MatchRequest {
    
    private Long playerId;
    private String gameMode;
    private int rank;
    private int eloRating;
    private Map<String, Object> preferences;
    private LocalDateTime requestTime;
    private int timeoutSeconds;
    private boolean priorityQueue;

    public MatchRequest() {
        this.requestTime = LocalDateTime.now();
        this.timeoutSeconds = 300; // Default 5 minutes
    }

    public MatchRequest(Long playerId, String gameMode, int rank, int eloRating) {
        this();
        this.playerId = playerId;
        this.gameMode = gameMode;
        this.rank = rank;
        this.eloRating = eloRating;
    }

    // Getters and Setters
    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getEloRating() {
        return eloRating;
    }

    public void setEloRating(int eloRating) {
        this.eloRating = eloRating;
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Object> preferences) {
        this.preferences = preferences;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isPriorityQueue() {
        return priorityQueue;
    }

    public void setPriorityQueue(boolean priorityQueue) {
        this.priorityQueue = priorityQueue;
    }

    /**
     * Check if request has expired
     */
    public boolean isExpired() {
        return requestTime.plusSeconds(timeoutSeconds).isBefore(LocalDateTime.now());
    }

    /**
     * Get waiting time in seconds
     */
    public long getWaitingTime() {
        return java.time.Duration.between(requestTime, LocalDateTime.now()).getSeconds();
    }

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