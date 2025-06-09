package com.game.service.gateway.handler;

import com.game.common.Constants;
import com.game.frame.netty.handler.MessageHandler;
import com.game.frame.netty.protocol.MessageWrapper;
import com.game.frame.netty.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Client message handler processing login, heartbeat, and business message forwarding
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
/**
 * ClientMessage处理器
 * 
 * 功能说明：
 * - 处理特定类型的请求或事件
 * - 实现消息路由和业务逻辑
 * - 提供异步处理和错误处理
 *
 * @author lx
 * @date 2024-01-01
 */
public class ClientMessageHandler implements MessageHandler<MessageWrapper> {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientMessageHandler.class);
    
    @Autowired
    private LoginMessageHandler loginMessageHandler;
    
    @Autowired
    private GameMessageHandler gameMessageHandler;
    
    @Override
    public void handle(Session session, MessageWrapper message) {
        int messageId = message.getMessageId();
        
        logger.debug("Processing message ID {} from session {}", messageId, session.getSessionId());
        
        switch (messageId) {
            case Constants.MSG_HEARTBEAT_REQUEST:
                gameMessageHandler.handleHeartbeat(session, message);
                break;
            case Constants.MSG_LOGIN_REQUEST:
                loginMessageHandler.handleLogin(session, message);
                break;
            default:
                handleBusinessMessage(session, message);
                break;
        }
    }
    
    /**
     * Handles business messages by routing them to appropriate game handlers
     * 
     * @param session client session
     * @param message business message
     */
    private void handleBusinessMessage(Session session, MessageWrapper message) {
        if (!session.isAuthenticated()) {
            logger.warn("Unauthenticated session {} tried to send business message: {}", 
                    session.getSessionId(), message.getMessageId());
            return;
        }
        
        logger.debug("Processing business message {} from user {} (session: {})", 
                message.getMessageId(), session.getUserId(), session.getSessionId());
        
        // Route to appropriate game handler based on message ID
        // Add more message types as needed
        switch (message.getMessageId()) {
            case 1001: // Enter game
                gameMessageHandler.handleEnterGame(session, message);
                break;
            case 1002: // Exit game
                gameMessageHandler.handleExitGame(session, message);
                break;
            case 1003: // Sync game data
                gameMessageHandler.handleGameDataSync(session, message);
                break;
            default:
                logger.warn("Unknown business message type: {}", message.getMessageId());
                break;
        }
    }
    
    @Override
    public Class<MessageWrapper> getMessageType() {
        return MessageWrapper.class;
    }
    
    @Override
    public int getMessageId() {
        // This handler processes all message types
        return -1;
    }
    
    @Override
    public boolean requiresAuthentication() {
        return false; // This handler does its own authentication checks
    }
}