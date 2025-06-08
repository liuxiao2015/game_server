package com.game.service.logic.service;

import com.game.common.api.service.IGameService;
import com.game.common.model.exception.ErrorCode;
import com.game.common.model.response.Result;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Game service implementation
 * Provides game entering, exiting and data synchronization operations
 *
 * @author lx
 * @date 2024-01-01
 */
@DubboService(version = "1.0.0", group = "game", timeout = 3000)
public class GameServiceImpl implements IGameService {

    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);

    // Mock game status storage
    private final ConcurrentHashMap<Long, GameStatus> userGameStatus = new ConcurrentHashMap<>();

    @Override
    public Result<GameEnterData> enterGame(Long userId, String gameType) {
        try {
            if (userId == null) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "User ID cannot be null");
            }
            if (gameType == null || gameType.trim().isEmpty()) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "Game type cannot be empty");
            }

            // Check if user is already in a game
            GameStatus currentStatus = userGameStatus.get(userId);
            if (currentStatus != null && "IN_GAME".equals(currentStatus.getStatus())) {
                return Result.failure(ErrorCode.BUSINESS_ERROR, "User is already in a game");
            }

            // Create game session
            String gameSessionId = UUID.randomUUID().toString();
            String gameServerHost = "127.0.0.1"; // Mock game server
            Integer gameServerPort = 9999;

            GameEnterData enterData = new GameEnterData(gameSessionId, gameServerHost, gameServerPort);

            // Update user game status
            GameStatus gameStatus = new GameStatus(gameType, "IN_GAME", gameSessionId);
            userGameStatus.put(userId, gameStatus);

            logger.info("User {} entered game: type={}, sessionId={}", userId, gameType, gameSessionId);
            return Result.success(enterData);

        } catch (Exception e) {
            logger.error("Failed to enter game: userId={}, gameType={}", userId, gameType, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to enter game: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> exitGame(Long userId) {
        try {
            if (userId == null) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "User ID cannot be null");
            }

            GameStatus gameStatus = userGameStatus.get(userId);
            if (gameStatus == null) {
                return Result.failure(ErrorCode.BUSINESS_ERROR, "User is not in any game");
            }

            // Update status to IDLE
            gameStatus.setStatus("IDLE");
            gameStatus.setLastActiveTime(System.currentTimeMillis());

            logger.info("User {} exited game: gameType={}", userId, gameStatus.getGameType());
            return Result.success();

        } catch (Exception e) {
            logger.error("Failed to exit game: userId={}", userId, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to exit game: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> syncGameData(Long userId, String gameData) {
        try {
            if (userId == null) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "User ID cannot be null");
            }
            if (gameData == null) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "Game data cannot be null");
            }

            GameStatus gameStatus = userGameStatus.get(userId);
            if (gameStatus == null || !"IN_GAME".equals(gameStatus.getStatus())) {
                return Result.failure(ErrorCode.BUSINESS_ERROR, "User is not in a game");
            }

            // Update last active time
            gameStatus.setLastActiveTime(System.currentTimeMillis());

            // In real implementation, save game data to database
            logger.debug("Synced game data for user {}: dataLength={}", userId, gameData.length());
            return Result.success();

        } catch (Exception e) {
            logger.error("Failed to sync game data: userId={}", userId, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to sync game data: " + e.getMessage());
        }
    }

    @Override
    public Result<GameStatus> getUserGameStatus(Long userId) {
        try {
            if (userId == null) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "User ID cannot be null");
            }

            GameStatus gameStatus = userGameStatus.get(userId);
            if (gameStatus == null) {
                // Create default IDLE status
                gameStatus = new GameStatus(null, "IDLE", null);
            }

            return Result.success(gameStatus);

        } catch (Exception e) {
            logger.error("Failed to get user game status: userId={}", userId, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to get user game status: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> heartbeat(Long userId) {
        try {
            if (userId == null) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "User ID cannot be null");
            }

            GameStatus gameStatus = userGameStatus.get(userId);
            if (gameStatus != null) {
                gameStatus.setLastActiveTime(System.currentTimeMillis());
                logger.debug("Heartbeat received from user {}", userId);
            }

            return Result.success();

        } catch (Exception e) {
            logger.error("Failed to process heartbeat: userId={}", userId, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to process heartbeat: " + e.getMessage());
        }
    }
}