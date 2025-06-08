package com.game.frame.netty.handler;

import com.game.common.Constants;
import com.game.frame.concurrent.VirtualThreadExecutor;
import com.game.frame.netty.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Message dispatcher providing message ID-based routing, handler registration, 
 * virtual thread processing, and async response support
 *
 * @author lx
 * @date 2024-01-01
 */
public class MessageDispatcher {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);
    
    private final HandlerRegistry handlerRegistry;
    private final VirtualThreadExecutor executor;
    
    /**
     * Creates a new MessageDispatcher
     * 
     * @param handlerRegistry the handler registry
     * @param executor the thread executor
     */
    public MessageDispatcher(HandlerRegistry handlerRegistry, VirtualThreadExecutor executor) {
        this.handlerRegistry = handlerRegistry;
        this.executor = executor;
    }
    
    /**
     * Creates a new MessageDispatcher with default executor
     * 
     * @param handlerRegistry the handler registry
     */
    public MessageDispatcher(HandlerRegistry handlerRegistry) {
        this(handlerRegistry, new VirtualThreadExecutor(Constants.THREAD_POOL_BUSINESS));
    }
    
    /**
     * Dispatches a message to the appropriate handler
     * 
     * @param session the session that sent the message
     * @param messageId the message ID
     * @param message the message object
     */
    public void dispatch(Session session, int messageId, Object message) {
        // Update session activity
        session.updateActiveTime();
        
        // Get handler for message ID
        MessageHandler<?> handler = handlerRegistry.getHandler(messageId);
        if (handler == null) {
            logger.warn("No handler found for message ID: {}", messageId);
            return;
        }
        
        // Check authentication requirement
        if (handler.requiresAuthentication() && !session.isAuthenticated()) {
            logger.warn("Unauthenticated session tried to send message requiring auth: {} from {}", 
                    messageId, session.getSessionId());
            return;
        }
        
        // Dispatch message in virtual thread
        executor.submit(() -> {
            try {
                long startTime = System.currentTimeMillis();
                
                // Type-safe message handling
                handleMessage(handler, session, message);
                
                long processingTime = System.currentTimeMillis() - startTime;
                if (processingTime > 100) { // Log slow processing
                    logger.warn("Slow message processing: ID={}, time={}ms, session={}", 
                            messageId, processingTime, session.getSessionId());
                }
                
            } catch (Exception e) {
                logger.error("Error processing message ID {} from session {}", 
                        messageId, session.getSessionId(), e);
            }
        });
    }
    
    /**
     * Type-safe message handling
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleMessage(MessageHandler handler, Session session, Object message) {
        // Verify message type
        if (!handler.getMessageType().isInstance(message)) {
            logger.error("Message type mismatch: expected {}, got {}", 
                    handler.getMessageType().getSimpleName(), message.getClass().getSimpleName());
            return;
        }
        
        // Handle the message
        handler.handle(session, message);
    }
    
    /**
     * Registers a message handler
     * 
     * @param handler the handler to register
     * @param <T> message type
     */
    public <T> void registerHandler(MessageHandler<T> handler) {
        handlerRegistry.registerHandler(handler);
    }
    
    /**
     * Gets handler information
     * 
     * @return handler information string
     */
    public String getHandlerInfo() {
        return handlerRegistry.getHandlerInfo();
    }
    
    /**
     * Shuts down the dispatcher
     */
    public void shutdown() {
        executor.shutdown();
        handlerRegistry.clear();
        logger.info("MessageDispatcher shutdown completed");
    }
}