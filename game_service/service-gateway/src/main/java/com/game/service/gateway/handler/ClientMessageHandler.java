package com.game.service.gateway.handler;

import com.game.common.Constants;
import com.game.frame.netty.handler.MessageHandler;
import com.game.frame.netty.protocol.MessageWrapper;
import com.game.frame.netty.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Client message handler processing login, heartbeat, and business message forwarding
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
public class ClientMessageHandler implements MessageHandler<MessageWrapper> {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientMessageHandler.class);
    
    @Override
    public void handle(Session session, MessageWrapper message) {
        int messageId = message.getMessageId();
        
        logger.debug("Processing message ID {} from session {}", messageId, session.getSessionId());
        
        switch (messageId) {
            case Constants.MSG_HEARTBEAT_REQUEST:
                handleHeartbeat(session, message);
                break;
            case Constants.MSG_LOGIN_REQUEST:
                handleLogin(session, message);
                break;
            default:
                handleBusinessMessage(session, message);
                break;
        }
    }
    
    /**
     * Handles heartbeat requests
     * 
     * @param session client session
     * @param message heartbeat message
     */
    private void handleHeartbeat(Session session, MessageWrapper message) {
        logger.debug("Received heartbeat from session: {}", session.getSessionId());
        
        // For now, just update session activity time
        session.updateActiveTime();
        
        // TODO: Send heartbeat response
        logger.debug("Heartbeat processed for session: {}", session.getSessionId());
    }
    
    /**
     * Handles login requests
     * 
     * @param session client session
     * @param message login message
     */
    private void handleLogin(Session session, MessageWrapper message) {
        logger.info("Received login request from session: {}", session.getSessionId());
        
        // For now, just mark as authenticated
        // TODO: Implement proper authentication logic
        String userId = "user_" + System.currentTimeMillis();
        session.setUserId(userId);
        session.setAuthenticated(true);
        
        logger.info("User {} logged in successfully from session: {}", userId, session.getSessionId());
    }
    
    /**
     * Handles business messages
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
        
        // TODO: Forward to appropriate business service
        logger.debug("Business message processed");
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