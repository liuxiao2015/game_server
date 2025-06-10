package com.game.service.chat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.service.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chat WebSocket handler
 * Handles WebSocket connections for real-time chat
 *
 * @author lx
 * @date 2025/01/08
 */
@Component
/**
 * ChatWebSocket处理器
 * 
 * 功能说明：
 * - 处理特定类型的请求或事件
 * - 实现消息路由和业务逻辑
 * - 提供异步处理和错误处理
 *
 * @author lx
 * @date 2024-01-01
 */
public class ChatWebSocketHandler implements WebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private WebSocketSessionManager sessionManager;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());
        
        // Extract user ID from session attributes or headers
        Long userId = extractUserId(session);
        if (userId != null) {
            sessionManager.addSession(userId, session);
            logger.info("User {} connected via WebSocket", userId);
        } else {
            logger.warn("Connection without valid user ID, closing session: {}", session.getId());
            session.close(CloseStatus.BAD_DATA.withReason("Invalid user authentication"));
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            handleTextMessage(session, (TextMessage) message);
        } else {
            logger.warn("Unsupported message type: {}", message.getClass().getSimpleName());
        }
    }

    private void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            logger.debug("Received message: {}", payload);
            
            // Parse message as JSON
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String messageType = (String) messageData.get("type");
            
            Long userId = sessionManager.getUserId(session);
            if (userId == null) {
                logger.warn("Message from unauthenticated session: {}", session.getId());
                return;
            }
            
            // Handle different message types
            switch (messageType) {
                case "CHAT_MESSAGE":
                    handleChatMessage(userId, messageData);
                    break;
                case "JOIN_CHANNEL":
                    handleJoinChannel(userId, messageData);
                    break;
                case "LEAVE_CHANNEL":
                    handleLeaveChannel(userId, messageData);
                    break;
                case "HEARTBEAT":
                    handleHeartbeat(session, userId);
                    break;
                default:
                    logger.warn("Unknown message type: {}", messageType);
            }
            
        } catch (Exception e) {
            logger.error("Error handling message from session {}: {}", session.getId(), e.getMessage(), e);
        }
    }

    private void handleChatMessage(Long userId, Map<String, Object> messageData) {
        try {
            Long channelId = Long.valueOf(messageData.get("channelId").toString());
            String content = (String) messageData.get("content");
            String messageType = (String) messageData.getOrDefault("messageType", "TEXT");
            
            chatService.sendMessage(userId, channelId, content, messageType);
            
        } catch (Exception e) {
            logger.error("Error handling chat message from user {}: {}", userId, e.getMessage(), e);
        }
    }

    private void handleJoinChannel(Long userId, Map<String, Object> messageData) {
        try {
            Long channelId = Long.valueOf(messageData.get("channelId").toString());
            chatService.joinChannel(userId, channelId);
            
        } catch (Exception e) {
            logger.error("Error handling join channel from user {}: {}", userId, e.getMessage(), e);
        }
    }

    private void handleLeaveChannel(Long userId, Map<String, Object> messageData) {
        try {
            Long channelId = Long.valueOf(messageData.get("channelId").toString());
            chatService.leaveChannel(userId, channelId);
            
        } catch (Exception e) {
            logger.error("Error handling leave channel from user {}: {}", userId, e.getMessage(), e);
        }
    }

    private void handleHeartbeat(WebSocketSession session, Long userId) {
        try {
            // Update session activity
            sessionManager.updateActivity(userId);
            
            // Send heartbeat response
            Map<String, Object> response = Map.of(
                "type", "HEARTBEAT_RESPONSE",
                "timestamp", System.currentTimeMillis()
            );
            sendMessage(session, response);
            
        } catch (Exception e) {
            logger.error("Error handling heartbeat from user {}: {}", userId, e.getMessage(), e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        
        Long userId = sessionManager.getUserId(session);
        if (userId != null) {
            sessionManager.removeSession(userId);
            logger.info("User {} disconnected from WebSocket", userId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Extract user ID from WebSocket session
     */
    private Long extractUserId(WebSocketSession session) {
        // Try to get user ID from query parameters
        String query = session.getUri().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                    try {
                        return Long.valueOf(keyValue[1]);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid userId format: {}", keyValue[1]);
                    }
                }
            }
        }
        
        // Try to get from session attributes
        Object userIdAttr = session.getAttributes().get("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        }
        
        return null;
    }

    /**
     * Send message to WebSocket session
     */
    private void sendMessage(WebSocketSession session, Object message) {
        try {
            if (session.isOpen()) {
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
            }
        } catch (Exception e) {
            logger.error("Error sending message to session {}: {}", session.getId(), e.getMessage(), e);
        }
    }
}