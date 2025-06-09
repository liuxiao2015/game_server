package com.game.test.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏客户端消息处理器
 * @author lx
 * @date 2025/06/08
 */
public class GameClientHandler extends SimpleChannelInboundHandler<Object> {
    
    private static final Logger logger = LoggerFactory.getLogger(GameClientHandler.class);
    
    private final GameTestClient client;
    
    public GameClientHandler(GameTestClient client) {
        this.client = client;
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel active: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel inactive: {}", ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("Received message: {}", msg);
        
        // 处理接收到的消息
        // TODO: 根据实际协议解析消息
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception caught in client handler", cause);
        ctx.close();
    }
}