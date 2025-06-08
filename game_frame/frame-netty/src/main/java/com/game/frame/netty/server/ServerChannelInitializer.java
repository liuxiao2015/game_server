package com.game.frame.netty.server;

import com.game.frame.netty.config.NettyServerConfig;
import com.game.frame.netty.handler.MessageDispatcher;
import com.game.frame.netty.protocol.ProtobufDecoder;
import com.game.frame.netty.protocol.ProtobufEncoder;
import com.game.frame.netty.session.SessionManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * Channel pipeline initializer with message length decoder, Protobuf decoder, 
 * heartbeat handler, business handler, and exception handler
 *
 * @author lx
 * @date 2024-01-01
 */
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    private final SessionManager sessionManager;
    private final MessageDispatcher messageDispatcher;
    private final NettyServerConfig config;
    
    /**
     * Creates a new ServerChannelInitializer
     * 
     * @param sessionManager session manager
     * @param messageDispatcher message dispatcher
     * @param config server configuration
     */
    public ServerChannelInitializer(SessionManager sessionManager, MessageDispatcher messageDispatcher, NettyServerConfig config) {
        this.sessionManager = sessionManager;
        this.messageDispatcher = messageDispatcher;
        this.config = config;
    }
    
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        // Length field based frame decoder (4 bytes length + content)
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
                1024 * 1024,  // Max frame length (1MB)
                0,             // Length field offset
                4,             // Length field length
                0,             // Length adjustment
                4              // Initial bytes to strip
        ));
        
        // Length field prepender for outgoing messages
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        
        // Protobuf decoder
        pipeline.addLast("protobufDecoder", new ProtobufDecoder());
        
        // Protobuf encoder
        pipeline.addLast("protobufEncoder", new ProtobufEncoder());
        
        // Idle state handler for heartbeat detection
        pipeline.addLast("idleStateHandler", new IdleStateHandler(
                config.getHeartbeatTimeout() / 1000,  // Reader idle time
                0,                                     // Writer idle time
                0,                                     // All idle time
                TimeUnit.SECONDS
        ));
        
        // Heartbeat handler
        pipeline.addLast("heartbeatHandler", new HeartbeatHandler());
        
        // Business message handler
        pipeline.addLast("businessHandler", new BusinessHandler(sessionManager, messageDispatcher));
        
        // Exception handler (should be last)
        pipeline.addLast("exceptionHandler", new ExceptionHandler());
    }
}