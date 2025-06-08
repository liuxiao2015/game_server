package com.game.frame.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception handler for catching and logging channel exceptions
 *
 * @author lx
 * @date 2024-01-01
 */
public class ExceptionHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Channel exception caught: {}", ctx.channel().remoteAddress(), cause);
        
        // Close the channel on exception
        if (ctx.channel().isActive()) {
            ctx.close();
        }
    }
}