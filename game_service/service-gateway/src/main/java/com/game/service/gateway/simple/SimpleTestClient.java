package com.game.service.gateway.simple;

import com.game.common.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Simple test client to verify server functionality
 *
 * @author lx
 * @date 2024-01-01
 */
/**
 * Simple测试类Client
 * 
 * 功能说明：
 * - 验证对应功能模块的正确性
 * - 提供单元测试和集成测试用例
 * - 确保代码质量和功能稳定性
 * 
 * 测试范围：
 * - 核心业务逻辑的功能验证
 * - 边界条件和异常情况测试
 * - 性能和并发安全性测试
 *
 * @author lx
 * @date 2024-01-01
 */
public class SimpleTestClient {
    
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Test Client...");
        
        EventLoopGroup group = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // Length field prepender
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            
                            // Simple handler
                            pipeline.addLast("clientHandler", new SimpleChannelInboundHandlerAdapter<ByteBuf>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    System.out.println("Connected to server!");
                                    
                                    // Send a heartbeat message
                                    sendHeartbeat(ctx);
                                }
                                
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                    System.out.println("Received response from server");
                                }
                                
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
                        }
                    });
            
            // Connect to server
            ChannelFuture future = bootstrap.connect("localhost", 8888).sync();
            System.out.println("Client connected to localhost:8888");
            
            // Wait for connection to close
            future.channel().closeFuture().sync();
            
        } finally {
            group.shutdownGracefully();
        }
    }
    
    private static void sendHeartbeat(ChannelHandlerContext ctx) {
        // Create a simple heartbeat message
        // Format: messageId (4 bytes) + payloadLength (4 bytes) + payload
        ByteBuf buffer = Unpooled.buffer();
        
        // Message ID (heartbeat)
        buffer.writeInt(Constants.MSG_HEARTBEAT_REQUEST);
        
        // Payload length (0 for simple heartbeat)
        buffer.writeInt(0);
        
        // No payload for heartbeat
        
        ctx.writeAndFlush(buffer);
        System.out.println("Sent heartbeat message");
    }
    
    private static abstract class SimpleChannelInboundHandlerAdapter<I> extends ChannelInboundHandlerAdapter {
        
        @Override
        public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            boolean release = true;
            try {
                if (acceptInboundMessage(msg)) {
                    @SuppressWarnings("unchecked")
                    I imsg = (I) msg;
                    channelRead0(ctx, imsg);
                } else {
                    release = false;
                    ctx.fireChannelRead(msg);
                }
            } finally {
                if (release) {
                    if (msg instanceof ByteBuf) {
                        ((ByteBuf) msg).release();
                    }
                }
            }
        }
        
        protected boolean acceptInboundMessage(Object msg) throws Exception {
            return msg instanceof ByteBuf;
        }
        
        protected abstract void channelRead0(ChannelHandlerContext ctx, I msg) throws Exception;
    }
}