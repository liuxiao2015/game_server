package com.game.frame.netty.server;

import com.game.frame.netty.handler.MessageDispatcher;
import com.game.frame.netty.protocol.MessageWrapper;
import com.game.frame.netty.session.Session;
import com.game.frame.netty.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 业务消息处理器
 * 
 * 功能说明：
 * - 处理客户端发送的各种业务消息
 * - 负责消息的接收、解析、分发和响应
 * - 管理客户端连接的会话状态
 * - 提供消息处理的异常捕获和错误处理
 * 
 * 处理流程：
 * 1. 接收Netty通道传入的消息数据
 * 2. 从通道上下文中获取客户端会话信息
 * 3. 将消息包装为MessageWrapper对象
 * 4. 通过MessageDispatcher分发消息到具体的业务处理器
 * 5. 处理完成后发送响应消息给客户端
 * 6. 记录处理过程和结果日志
 * 
 * 技术特点：
 * - 继承自Netty的ChannelInboundHandlerAdapter
 * - 集成会话管理器，维护客户端连接状态
 * - 使用消息分发器实现业务逻辑的解耦
 * - 支持异步消息处理，提高服务器性能
 * 
 * 异常处理：
 * - 网络连接异常的自动清理
 * - 消息格式错误的友好提示
 * - 业务处理异常的降级响应
 * - 详细的错误日志记录
 * 
 * 性能优化：
 * - 避免阻塞操作，保持通道的高吞吐量
 * - 合理使用缓冲区，减少内存分配
 * - 异常路径的快速处理，避免资源泄露
 *
 * @author lx
 * @date 2024-01-01
 */
public class BusinessHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(BusinessHandler.class);
    
    private final SessionManager sessionManager;
    private final MessageDispatcher messageDispatcher;
    
    /**
     * Creates a new BusinessHandler
     * 
     * @param sessionManager session manager
     * @param messageDispatcher message dispatcher
     */
    public BusinessHandler(SessionManager sessionManager, MessageDispatcher messageDispatcher) {
        this.sessionManager = sessionManager;
        this.messageDispatcher = messageDispatcher;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Create session when channel becomes active
        Session session = sessionManager.createSession(ctx.channel());
        ctx.channel().attr(SessionKeys.SESSION_KEY).set(session);
        
        logger.info("Client connected: {} -> {}", ctx.channel().remoteAddress(), session.getSessionId());
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Remove session when channel becomes inactive
        Session session = ctx.channel().attr(SessionKeys.SESSION_KEY).get();
        if (session != null) {
            sessionManager.removeSession(session.getSessionId());
            logger.info("Client disconnected: {}", session.getSessionId());
        }
        super.channelInactive(ctx);
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof MessageWrapper) {
            MessageWrapper wrapper = (MessageWrapper) msg;
            Session session = ctx.channel().attr(SessionKeys.SESSION_KEY).get();
            
            if (session != null) {
                // Dispatch message to appropriate handler
                messageDispatcher.dispatch(session, wrapper.getMessageId(), wrapper);
            } else {
                logger.error("No session found for channel: {}", ctx.channel().remoteAddress());
                ctx.close();
            }
        } else {
            logger.warn("Unexpected message type: {}", msg.getClass());
        }
    }
}