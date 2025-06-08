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
 * Business message handler processing client messages
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