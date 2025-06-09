package com.game.test.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 游戏测试客户端
 * @author lx
 * @date 2025/06/08
 */
public class GameTestClient {
    
    private static final Logger logger = LoggerFactory.getLogger(GameTestClient.class);
    
    private String host;
    private int port;
    private Channel channel;
    private EventLoopGroup workerGroup;
    private boolean connected = false;
    
    public GameTestClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    /**
     * 连接服务器
     */
    public CompletableFuture<Boolean> connect() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        workerGroup = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            
                            // 添加编解码器
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            
                            // 添加消息处理器
                            pipeline.addLast("messageHandler", new GameClientHandler(GameTestClient.this));
                        }
                    });
            
            // 连接服务器
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channel = channelFuture.channel();
            connected = true;
            
            logger.info("Connected to game server {}:{}", host, port);
            future.complete(true);
            
        } catch (Exception e) {
            logger.error("Failed to connect to game server {}:{}", host, port, e);
            future.complete(false);
        }
        
        return future;
    }
    
    /**
     * 发送消息
     */
    public CompletableFuture<Response> sendMessage(Message message) {
        CompletableFuture<Response> future = new CompletableFuture<>();
        
        if (!connected || channel == null || !channel.isActive()) {
            future.completeExceptionally(new IllegalStateException("Client not connected"));
            return future;
        }
        
        try {
            // 发送消息并等待响应
            channel.writeAndFlush(message).addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                    logger.debug("Message sent successfully: {}", message.getType());
                    
                    // 模拟响应（实际应该从服务器接收）
                    Response response = new Response(message.getId(), "SUCCESS", "Message processed successfully");
                    future.complete(response);
                } else {
                    logger.error("Failed to send message: {}", message.getType());
                    future.completeExceptionally(channelFuture.cause());
                }
            });
            
        } catch (Exception e) {
            logger.error("Error sending message", e);
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * 模拟玩家行为
     */
    public CompletableFuture<Void> simulatePlayerBehavior(BehaviorScript script) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        logger.info("Starting player behavior simulation: {}", script.getName());
        
        // 异步执行行为脚本
        CompletableFuture.runAsync(() -> {
            try {
                for (BehaviorStep step : script.getSteps()) {
                    executeStep(step);
                    
                    // 步骤间延迟
                    if (step.getDelayMs() > 0) {
                        Thread.sleep(step.getDelayMs());
                    }
                }
                
                logger.info("Player behavior simulation completed: {}", script.getName());
                future.complete(null);
                
            } catch (Exception e) {
                logger.error("Error during behavior simulation", e);
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }
    
    /**
     * 执行行为步骤
     */
    private void executeStep(BehaviorStep step) {
        logger.debug("Executing step: {}", step.getAction());
        
        switch (step.getAction()) {
            case "LOGIN":
                sendLoginMessage(step.getParameters());
                break;
            case "MOVE":
                sendMoveMessage(step.getParameters());
                break;
            case "CHAT":
                sendChatMessage(step.getParameters());
                break;
            case "USE_ITEM":
                sendUseItemMessage(step.getParameters());
                break;
            case "BATTLE":
                sendBattleMessage(step.getParameters());
                break;
            default:
                logger.warn("Unknown action: {}", step.getAction());
        }
    }
    
    /**
     * 发送登录消息
     */
    private void sendLoginMessage(String parameters) {
        Message loginMessage = new Message("LOGIN", parameters);
        sendMessage(loginMessage);
    }
    
    /**
     * 发送移动消息
     */
    private void sendMoveMessage(String parameters) {
        Message moveMessage = new Message("MOVE", parameters);
        sendMessage(moveMessage);
    }
    
    /**
     * 发送聊天消息
     */
    private void sendChatMessage(String parameters) {
        Message chatMessage = new Message("CHAT", parameters);
        sendMessage(chatMessage);
    }
    
    /**
     * 发送使用物品消息
     */
    private void sendUseItemMessage(String parameters) {
        Message useItemMessage = new Message("USE_ITEM", parameters);
        sendMessage(useItemMessage);
    }
    
    /**
     * 发送战斗消息
     */
    private void sendBattleMessage(String parameters) {
        Message battleMessage = new Message("BATTLE", parameters);
        sendMessage(battleMessage);
    }
    
    /**
     * 断开连接
     */
    public void disconnect() {
        try {
            connected = false;
            
            if (channel != null && channel.isActive()) {
                channel.close().sync();
            }
            
            if (workerGroup != null) {
                workerGroup.shutdownGracefully(2, 5, TimeUnit.SECONDS);
            }
            
            logger.info("Disconnected from game server");
            
        } catch (Exception e) {
            logger.error("Error during disconnect", e);
        }
    }
    
    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return connected && channel != null && channel.isActive();
    }
    
    // 简单的消息类
    public static class Message {
        private String id;
        private String type;
        private String data;
        
        public Message(String type, String data) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.type = type;
            this.data = data;
        }
        
        // Getters
        public String getId() { return id; }
        public String getType() { return type; }
        public String getData() { return data; }
    }
    
    // 响应类
    public static class Response {
        private String messageId;
        private String status;
        private String data;
        
        public Response(String messageId, String status, String data) {
            this.messageId = messageId;
            this.status = status;
            this.data = data;
        }
        
        // Getters
        public String getMessageId() { return messageId; }
        public String getStatus() { return status; }
        public String getData() { return data; }
    }
}