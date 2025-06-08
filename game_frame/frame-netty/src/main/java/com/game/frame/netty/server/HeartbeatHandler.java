package com.game.frame.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Heartbeat handler with idle detection, heartbeat timeout disconnect, and heartbeat statistics
 *
 * @author lx
 * @date 2024-01-01
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleEvent = (IdleStateEvent) evt;
            
            switch (idleEvent.state()) {
                case READER_IDLE:
                    // No data received within the specified time, close the connection
                    logger.warn("Connection idle timeout, closing: {}", ctx.channel().remoteAddress());
                    ctx.close();
                    break;
                case WRITER_IDLE:
                    // Can send heartbeat here if needed
                    break;
                case ALL_IDLE:
                    logger.warn("Connection all idle, closing: {}", ctx.channel().remoteAddress());
                    ctx.close();
                    break;
                default:
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}