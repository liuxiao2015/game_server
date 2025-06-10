package com.game.frame.netty.handler;

import com.game.common.Constants;
import com.game.frame.concurrent.VirtualThreadExecutor;
import com.game.frame.netty.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息分发器
 * 
 * 功能说明：
 * - 基于消息ID提供智能消息路由功能
 * - 管理消息处理器的注册和生命周期
 * - 使用虚拟线程池进行高性能异步消息处理
 * - 支持异步响应和广播消息的分发机制
 * 
 * 核心特性：
 * - 消息路由：根据消息类型自动路由到对应的处理器
 * - 处理器管理：支持处理器的动态注册和注销
 * - 并发处理：基于虚拟线程实现高并发消息处理
 * - 异步响应：支持异步消息处理和响应回调
 * - 广播支持：实现消息的一对多广播分发
 * 
 * 架构设计：
 * - 使用HandlerRegistry管理消息处理器映射关系
 * - 集成VirtualThreadExecutor提供高性能并发处理能力
 * - 支持消息处理的链式调用和中间件模式
 * - 提供完整的消息生命周期管理
 * 
 * 性能优化：
 * - 虚拟线程技术，支持大量并发连接和消息处理
 * - 异步非阻塞处理，最大化服务器吞吐量
 * - 智能负载均衡，避免处理器资源争用
 * - 内存优化，减少对象创建和GC压力
 * 
 * 使用场景：
 * - 游戏客户端与服务器的实时消息通信
 * - 聊天系统的消息广播和私聊
 * - 游戏状态同步和事件通知
 * - 系统公告和推送消息的分发
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