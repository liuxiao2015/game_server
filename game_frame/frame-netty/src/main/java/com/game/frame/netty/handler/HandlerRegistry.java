package com.game.frame.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Handler registry providing automatic scanning, manual registration, and handler management
 *
 * @author lx
 * @date 2024-01-01
 */
/**
 * 处理器Registry
 * 
 * 功能说明：
 * - 处理特定类型的请求或事件
 * - 实现消息路由和业务逻辑
 * - 提供异步处理和错误处理
 *
 * @author lx
 * @date 2024-01-01
 */
public class HandlerRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(HandlerRegistry.class);
    
    private final ConcurrentHashMap<Integer, MessageHandler<?>> handlers = new ConcurrentHashMap<>();
    
    /**
     * Registers a message handler
     * 
     * @param handler the message handler
     * @param <T> message type
     */
    public <T> void registerHandler(MessageHandler<T> handler) {
        int messageId = handler.getMessageId();
        MessageHandler<?> existing = handlers.put(messageId, handler);
        
        if (existing != null) {
            logger.warn("Replaced existing handler for message ID {}: {} -> {}", 
                    messageId, existing.getClass().getSimpleName(), handler.getClass().getSimpleName());
        }
        
        logger.info("Registered handler for message ID {}: {}", messageId, handler.getClass().getSimpleName());
    }
    
    /**
     * Gets a message handler by message ID
     * 
     * @param messageId message ID
     * @return message handler or null if not found
     */
    public MessageHandler<?> getHandler(int messageId) {
        return handlers.get(messageId);
    }
    
    /**
     * Removes a message handler by message ID
     * 
     * @param messageId message ID
     * @return removed handler or null
     */
    public MessageHandler<?> removeHandler(int messageId) {
        MessageHandler<?> handler = handlers.remove(messageId);
        if (handler != null) {
            logger.info("Removed handler for message ID {}: {}", messageId, handler.getClass().getSimpleName());
        }
        return handler;
    }
    
    /**
     * Checks if a handler is registered for the given message ID
     * 
     * @param messageId message ID
     * @return true if handler exists
     */
    public boolean hasHandler(int messageId) {
        return handlers.containsKey(messageId);
    }
    
    /**
     * Gets the number of registered handlers
     * 
     * @return handler count
     */
    public int getHandlerCount() {
        return handlers.size();
    }
    
    /**
     * Clears all registered handlers
     */
    public void clear() {
        int count = handlers.size();
        handlers.clear();
        logger.info("Cleared {} handlers", count);
    }
    
    /**
     * Gets information about all registered handlers
     * 
     * @return handler information string
     */
    public String getHandlerInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Registered Handlers (").append(handlers.size()).append("):\n");
        
        handlers.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e1.getKey(), e2.getKey()))
                .forEach(entry -> {
                    MessageHandler<?> handler = entry.getValue();
                    sb.append("  ").append(entry.getKey())
                            .append(" -> ").append(handler.getClass().getSimpleName())
                            .append(" (").append(handler.getMessageType().getSimpleName()).append(")")
                            .append(handler.requiresAuthentication() ? " [Auth Required]" : "")
                            .append("\n");
                });
        
        return sb.toString();
    }
}