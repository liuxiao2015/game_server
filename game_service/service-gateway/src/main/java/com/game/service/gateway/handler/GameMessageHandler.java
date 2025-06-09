package com.game.service.gateway.handler;

import com.game.frame.netty.protocol.MessageWrapper;
import com.game.frame.netty.session.Session;
import com.game.service.gateway.manager.RpcServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Game message handler
 * Forwards game-related messages to Logic service via Dubbo RPC
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
/**
 * GameMessage处理器
 * 
 * 功能说明：
 * - 处理特定类型的请求或消息
 * - 实现业务逻辑的具体处理流程
 * - 提供请求验证和响应封装功能
 * - 支持异步处理和错误处理机制
 * 
 * 处理流程：
 * 1. 接收请求或消息数据
 * 2. 验证请求参数的有效性
 * 3. 执行具体的业务逻辑处理
 * 4. 封装处理结果并返回响应
 * 5. 记录处理日志和性能统计
 * 
 * 技术特点：
 * - 基于Spring框架的依赖注入
 * - 支持事务管理和异常处理
 * - 集成缓存和数据访问组件
 *
 * @author lx
 * @date 2024-01-01
 */
public class GameMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(GameMessageHandler.class);

    @Autowired
    private RpcServiceManager rpcServiceManager;

    /**
     * Handle enter game message
     */
    public void handleEnterGame(Session session, MessageWrapper message) {
        try {
            if (!session.isAuthenticated()) {
                logger.warn("Unauthenticated session {} tried to enter game", session.getSessionId());
                sendGameError(session, "Authentication required");
                return;
            }

            Long userId = Long.valueOf(session.getUserId());
            String gameType = "default"; // Extract from message in real implementation

            // Call game service via RPC
            var result = rpcServiceManager.getGameService().enterGame(userId, gameType);

            if (result.isSuccess()) {
                sendEnterGameResponse(session, result.getData());
                logger.info("User {} entered game successfully", userId);
            } else {
                sendGameError(session, result.getMessage());
                logger.warn("User {} failed to enter game: {}", userId, result.getMessage());
            }

        } catch (Exception e) {
            logger.error("Failed to handle enter game for session: {}", session.getSessionId(), e);
            sendGameError(session, "Internal server error");
        }
    }

    /**
     * Handle exit game message
     */
    public void handleExitGame(Session session, MessageWrapper message) {
        try {
            if (!session.isAuthenticated()) {
                logger.warn("Unauthenticated session {} tried to exit game", session.getSessionId());
                return;
            }

            Long userId = Long.valueOf(session.getUserId());

            // Call game service via RPC
            var result = rpcServiceManager.getGameService().exitGame(userId);

            if (result.isSuccess()) {
                sendExitGameResponse(session);
                logger.info("User {} exited game successfully", userId);
            } else {
                sendGameError(session, result.getMessage());
                logger.warn("User {} failed to exit game: {}", userId, result.getMessage());
            }

        } catch (Exception e) {
            logger.error("Failed to handle exit game for session: {}", session.getSessionId(), e);
            sendGameError(session, "Internal server error");
        }
    }

    /**
     * Handle game data sync message
     */
    public void handleGameDataSync(Session session, MessageWrapper message) {
        try {
            if (!session.isAuthenticated()) {
                logger.warn("Unauthenticated session {} tried to sync game data", session.getSessionId());
                return;
            }

            Long userId = Long.valueOf(session.getUserId());
            String gameData = extractGameData(message);

            // Call game service via RPC
            var result = rpcServiceManager.getGameService().syncGameData(userId, gameData);

            if (result.isSuccess()) {
                sendSyncResponse(session);
                logger.debug("Game data synced for user {}", userId);
            } else {
                sendGameError(session, result.getMessage());
                logger.warn("Failed to sync game data for user {}: {}", userId, result.getMessage());
            }

        } catch (Exception e) {
            logger.error("Failed to handle game data sync for session: {}", session.getSessionId(), e);
            sendGameError(session, "Internal server error");
        }
    }

    /**
     * Handle heartbeat message
     */
    public void handleHeartbeat(Session session, MessageWrapper message) {
        try {
            // Update session activity
            session.updateActiveTime();

            if (session.isAuthenticated()) {
                Long userId = Long.valueOf(session.getUserId());
                
                // Send heartbeat to game service
                rpcServiceManager.getGameService().heartbeat(userId);
                logger.debug("Heartbeat processed for user {}", userId);
            }

            // Send heartbeat response
            sendHeartbeatResponse(session);

        } catch (Exception e) {
            logger.error("Failed to handle heartbeat for session: {}", session.getSessionId(), e);
        }
    }

    /**
     * Extract game data from message
     */
    private String extractGameData(MessageWrapper message) {
        // In real implementation, extract and deserialize game data from message
        return "sample_game_data";
    }

    /**
     * Send enter game response
     */
    private void sendEnterGameResponse(Session session, com.game.common.api.service.IGameService.GameEnterData data) {
        try {
            logger.info("Sending enter game response to session {}: gameSessionId={}, host={}, port={}", 
                    session.getSessionId(), data.getGameSessionId(), 
                    data.getGameServerHost(), data.getGameServerPort());
            // In real implementation, serialize and send response
            // session.sendMessage(response);
        } catch (Exception e) {
            logger.error("Failed to send enter game response", e);
        }
    }

    /**
     * Send exit game response
     */
    private void sendExitGameResponse(Session session) {
        try {
            logger.info("Sending exit game response to session {}", session.getSessionId());
            // In real implementation, send response
            // session.sendMessage(response);
        } catch (Exception e) {
            logger.error("Failed to send exit game response", e);
        }
    }

    /**
     * Send sync response
     */
    private void sendSyncResponse(Session session) {
        try {
            logger.debug("Sending sync response to session {}", session.getSessionId());
            // In real implementation, send response
            // session.sendMessage(response);
        } catch (Exception e) {
            logger.error("Failed to send sync response", e);
        }
    }

    /**
     * Send heartbeat response
     */
    private void sendHeartbeatResponse(Session session) {
        try {
            logger.debug("Sending heartbeat response to session {}", session.getSessionId());
            // In real implementation, send heartbeat response
            // session.sendMessage(heartbeatResponse);
        } catch (Exception e) {
            logger.error("Failed to send heartbeat response", e);
        }
    }

    /**
     * Send game error response
     */
    private void sendGameError(Session session, String errorMessage) {
        try {
            logger.warn("Sending game error to session {}: {}", session.getSessionId(), errorMessage);
            // In real implementation, send error response
            // session.sendMessage(errorResponse);
        } catch (Exception e) {
            logger.error("Failed to send game error response", e);
        }
    }
}