package com.game.service.match.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Match result model
 * Represents the result of a match operation
 *
 * @author lx
 * @date 2025/01/08
 */
public class MatchResult {
    
    private String matchId;
    private MatchStatus status;
    private List<Long> playerIds;
    private String gameMode;
    private String gameServerHost;
    private Integer gameServerPort;
    private LocalDateTime matchTime;
    private int estimatedWaitTime;
    private Map<String, Object> gameConfig;
    private String errorMessage;

    /**
     * Match status enumeration
     */
    public enum MatchStatus {
        PENDING,        // Waiting for match
        MATCHED,        // Successfully matched
        TIMEOUT,        // Match request timed out
        CANCELLED,      // Match request cancelled
        ERROR           // Error occurred
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