package com.game.frame.netty.handler;

import com.game.frame.netty.session.Session;

/**
 * Message handler /**
 * defining
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
interface defining processing methods and supported message types
 *
 * @author lx
 * @date 2024-01-01
 */
public interface MessageHandler<T> {
    
    /**
     * Handles an incoming message
     * 
     * @param session the session that sent the message
     * @param message the message to handle
     */
    void handle(Session session, T message);
    
    /**
     * Gets the message type this handler supports
     * 
     * @return message class type
     */
    Class<T> getMessageType();
    
    /**
     * Gets the message ID this handler supports
     * 
     * @return message ID
     */
    int getMessageId();
    
    /**
     * Checks if this handler requires authentication
     * 
     * @return true if authentication required
     */
    default boolean requiresAuthentication() {
        return true;
    }
}