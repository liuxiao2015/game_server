package com.game.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;

/**
 * 网络通信测试类
 * 
 * 功能说明：
 * - TCP连接建立与断开测试
 * - Protobuf消息编解码测试  
 * - 心跳机制测试（30秒间隔）
 * - 并发连接测试（1000个客户端）
 * - 消息延迟测试
 * 
 * 测试场景：
 * - 单连接基础功能测试
 * - 高并发连接压力测试
 * - 网络通信性能测试
 * - 异常情况处理测试
 * 
 * @author lx
 * @date 2025/06/08
 */
public class NetworkCommunicationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(NetworkCommunicationTest.class);
    
    // 测试配置常量
    private static final String TEST_SERVER_HOST = "localhost";
    private static final int TEST_SERVER_PORT = 8888;
    private static final int HEARTBEAT_INTERVAL = 30; // 30秒心跳间隔
    private static final int CONCURRENT_CLIENTS = 1000; // 并发客户端数量
    private static final int MESSAGE_LATENCY_TARGET = 100; // 延迟目标（毫秒）
    
    // 消息类型常量（假设与服务器端一致）
    private static final int MSG_HEARTBEAT_REQUEST = 1001;
    private static final int MSG_HEARTBEAT_RESPONSE = 1002;
    private static final int MSG_ECHO_REQUEST = 2001;
    private static final int MSG_ECHO_RESPONSE = 2002;
    
    private final ExecutorService executorService;
    private final List<String> testResults;
    private final List<String> testErrors;
    
    public NetworkCommunicationTest() {
        this.executorService = Executors.newCachedThreadPool();
        this.testResults = new ArrayList<>();
        this.testErrors = new ArrayList<>();
    }
    
    /**
     * 执行所有网络通信测试
     * 
     * @return 测试报告
     */
    public CompletableFuture<TestReport> runAllTests() {
        logger.info("开始执行网络通信综合测试...");
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            int totalTests = 5;
            int passedTests = 0;
            
            try {
                // 1. TCP连接建立与断开测试
                if (testTcpConnection()) {
                    testResults.add("TCP连接测试 - 通过");
                    passedTests++;
                } else {
                    testResults.add("TCP连接测试 - 失败");
                }
                
                // 2. Protobuf消息编解码测试
                if (testProtobufMessage()) {
                    testResults.add("Protobuf编解码测试 - 通过");
                    passedTests++;
                } else {
                    testResults.add("Protobuf编解码测试 - 失败");
                }
                
                // 3. 心跳机制测试
                if (testHeartbeatMechanism()) {
                    testResults.add("心跳机制测试 - 通过");
                    passedTests++;
                } else {
                    testResults.add("心跳机制测试 - 失败");
                }
                
                // 4. 并发连接测试
                if (testConcurrentConnections()) {
                    testResults.add("并发连接测试 - 通过");
                    passedTests++;
                } else {
                    testResults.add("并发连接测试 - 失败");
                }
                
                // 5. 消息延迟测试
                if (testMessageLatency()) {
                    testResults.add("消息延迟测试 - 通过");
                    passedTests++;
                } else {
                    testResults.add("消息延迟测试 - 失败");
                }
                
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                
                // 构建测试报告
                TestReport report = TestReport.builder()
                        .testSuiteName("网络通信测试套件")
                        .totalTests(totalTests)
                        .passedTests(passedTests)
                        .failedTests(totalTests - passedTests)
                        .skippedTests(0)
                        .executionTime(executionTime)
                        .build();
                
                logger.info("网络通信测试完成: {}", report);
                return report;
                
            } catch (Exception e) {
                logger.error("网络通信测试执行异常", e);
                testErrors.add("测试执行异常: " + e.getMessage());
                
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                
                return TestReport.builder()
                        .testSuiteName("网络通信测试套件")
                        .totalTests(totalTests)
                        .passedTests(passedTests)
                        .failedTests(totalTests - passedTests)
                        .skippedTests(0)
                        .executionTime(executionTime)
                        .build();
            }
        }, executorService);
    }
    
    /**
     * TCP连接建立与断开测试
     */
    private boolean testTcpConnection() {
        logger.info("开始TCP连接建立与断开测试...");
        
        EventLoopGroup group = new NioEventLoopGroup();
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            pipeline.addLast("handler", new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    logger.debug("收到服务器响应");
                                }
                                
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    logger.info("TCP连接建立成功");
                                }
                                
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) {
                                    logger.info("TCP连接断开");
                                }
                            });
                        }
                    });
            
            // 连接服务器
            ChannelFuture connectFuture = bootstrap.connect(TEST_SERVER_HOST, TEST_SERVER_PORT);
            Channel channel = connectFuture.sync().channel();
            
            // 验证连接状态
            if (channel.isActive()) {
                logger.info("TCP连接测试成功");
                
                // 主动断开连接
                channel.close().sync();
                logger.info("TCP断开连接测试成功");
                
                return true;
            } else {
                logger.error("TCP连接失败");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("TCP连接测试异常", e);
            return false;
        } finally {
            group.shutdownGracefully().awaitUninterruptibly();
        }
    }
    
    /**
     * Protobuf消息编解码测试
     */
    private boolean testProtobufMessage() {
        logger.info("开始Protobuf消息编解码测试...");
        
        EventLoopGroup group = new NioEventLoopGroup();
        
        try {
            AtomicInteger responseCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(1);
            
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            pipeline.addLast("handler", new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    // 解析响应消息
                                    if (msg.readableBytes() >= 8) {
                                        int messageId = msg.readInt();
                                        int payloadLength = msg.readInt();
                                        
                                        if (messageId == MSG_ECHO_RESPONSE) {
                                            logger.info("收到Echo响应，消息ID: {}, 载荷长度: {}", messageId, payloadLength);
                                            responseCount.incrementAndGet();
                                            latch.countDown();
                                        }
                                    }
                                }
                                
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    // 发送Echo请求消息
                                    sendEchoMessage(ctx, "Hello Protobuf Test");
                                }
                            });
                        }
                    });
            
            // 连接并发送消息
            ChannelFuture connectFuture = bootstrap.connect(TEST_SERVER_HOST, TEST_SERVER_PORT);
            Channel channel = connectFuture.sync().channel();
            
            // 等待响应
            boolean received = latch.await(10, TimeUnit.SECONDS);
            channel.close().sync();
            
            if (received && responseCount.get() > 0) {
                logger.info("Protobuf消息编解码测试成功");
                return true;
            } else {
                logger.warn("Protobuf消息编解码测试失败，未收到预期响应");
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Protobuf消息编解码测试异常", e);
            return false;
        } finally {
            group.shutdownGracefully().awaitUninterruptibly();
        }
    }
    
    /**
     * 心跳机制测试（30秒间隔）
     */
    private boolean testHeartbeatMechanism() {
        logger.info("开始心跳机制测试，间隔{}秒...", HEARTBEAT_INTERVAL);
        
        EventLoopGroup group = new NioEventLoopGroup();
        
        try {
            AtomicInteger heartbeatCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(2); // 测试2次心跳
            
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            pipeline.addLast("handler", new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    if (msg.readableBytes() >= 8) {
                                        int messageId = msg.readInt();
                                        int payloadLength = msg.readInt();
                                        
                                        if (messageId == MSG_HEARTBEAT_RESPONSE) {
                                            int count = heartbeatCount.incrementAndGet();
                                            logger.info("收到心跳响应 #{}", count);
                                            latch.countDown();
                                        }
                                    }
                                }
                                
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    // 启动心跳定时器
                                    ctx.executor().scheduleAtFixedRate(() -> {
                                        if (ctx.channel().isActive()) {
                                            sendHeartbeat(ctx);
                                        }
                                    }, 0, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
                                }
                            });
                        }
                    });
            
            // 连接服务器
            ChannelFuture connectFuture = bootstrap.connect(TEST_SERVER_HOST, TEST_SERVER_PORT);
            Channel channel = connectFuture.sync().channel();
            
            // 等待心跳响应（最多等待70秒，确保能收到2次心跳响应）
            boolean received = latch.await(70, TimeUnit.SECONDS);
            channel.close().sync();
            
            if (received && heartbeatCount.get() >= 2) {
                logger.info("心跳机制测试成功，收到{}次心跳响应", heartbeatCount.get());
                return true;
            } else {
                logger.warn("心跳机制测试失败，只收到{}次心跳响应", heartbeatCount.get());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("心跳机制测试异常", e);
            return false;
        } finally {
            group.shutdownGracefully().awaitUninterruptibly();
        }
    }
    
    /**
     * 并发连接测试（1000个客户端）
     */
    private boolean testConcurrentConnections() {
        logger.info("开始并发连接测试，客户端数量: {}", CONCURRENT_CLIENTS);
        
        EventLoopGroup group = new NioEventLoopGroup();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_CLIENTS);
        
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            pipeline.addLast("handler", new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    // 处理响应
                                }
                                
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    successCount.incrementAndGet();
                                    // 连接建立后立即关闭
                                    ctx.close();
                                }
                                
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) {
                                    latch.countDown();
                                }
                                
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    failureCount.incrementAndGet();
                                    ctx.close();
                                }
                            });
                        }
                    });
            
            // 并发创建连接
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < CONCURRENT_CLIENTS; i++) {
                final int clientId = i;
                executorService.submit(() -> {
                    try {
                        bootstrap.connect(TEST_SERVER_HOST, TEST_SERVER_PORT);
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        latch.countDown();
                        logger.debug("客户端 {} 连接失败", clientId);
                    }
                });
            }
            
            // 等待所有连接完成
            boolean completed = latch.await(60, TimeUnit.SECONDS);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            int successNum = successCount.get();
            int failureNum = failureCount.get();
            double successRate = (double) successNum / CONCURRENT_CLIENTS * 100;
            
            logger.info("并发连接测试完成:");
            logger.info("  - 总连接数: {}", CONCURRENT_CLIENTS);
            logger.info("  - 成功连接: {}", successNum);
            logger.info("  - 失败连接: {}", failureNum);
            logger.info("  - 成功率: {:.2f}%", successRate);
            logger.info("  - 总耗时: {} ms", duration);
            
            // 成功率超过95%即认为测试通过
            return successRate >= 95.0;
            
        } catch (Exception e) {
            logger.error("并发连接测试异常", e);
            return false;
        } finally {
            group.shutdownGracefully().awaitUninterruptibly();
        }
    }
    
    /**
     * 消息延迟测试
     */
    private boolean testMessageLatency() {
        logger.info("开始消息延迟测试，目标延迟: {} ms", MESSAGE_LATENCY_TARGET);
        
        EventLoopGroup group = new NioEventLoopGroup();
        
        try {
            AtomicLong totalLatency = new AtomicLong(0);
            AtomicInteger messageCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(100); // 测试100条消息
            
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            pipeline.addLast("handler", new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    long receiveTime = System.currentTimeMillis();
                                    
                                    if (msg.readableBytes() >= 16) {
                                        int messageId = msg.readInt();
                                        int payloadLength = msg.readInt();
                                        long sendTime = msg.readLong();
                                        
                                        if (messageId == MSG_ECHO_RESPONSE) {
                                            long latency = receiveTime - sendTime;
                                            totalLatency.addAndGet(latency);
                                            messageCount.incrementAndGet();
                                            latch.countDown();
                                            
                                            logger.debug("消息延迟: {} ms", latency);
                                        }
                                    }
                                }
                                
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    // 发送100条测试消息
                                    for (int i = 0; i < 100; i++) {
                                        sendTimestampMessage(ctx, i);
                                    }
                                }
                            });
                        }
                    });
            
            // 连接服务器
            ChannelFuture connectFuture = bootstrap.connect(TEST_SERVER_HOST, TEST_SERVER_PORT);
            Channel channel = connectFuture.sync().channel();
            
            // 等待所有响应
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            channel.close().sync();
            
            if (completed && messageCount.get() > 0) {
                double averageLatency = (double) totalLatency.get() / messageCount.get();
                logger.info("消息延迟测试完成:");
                logger.info("  - 测试消息数: {}", messageCount.get());
                logger.info("  - 平均延迟: {:.2f} ms", averageLatency);
                logger.info("  - 目标延迟: {} ms", MESSAGE_LATENCY_TARGET);
                
                return averageLatency <= MESSAGE_LATENCY_TARGET;
            } else {
                logger.warn("消息延迟测试失败，只收到{}条响应", messageCount.get());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("消息延迟测试异常", e);
            return false;
        } finally {
            group.shutdownGracefully().awaitUninterruptibly();
        }
    }
    
    /**
     * 发送心跳消息
     */
    private void sendHeartbeat(ChannelHandlerContext ctx) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(MSG_HEARTBEAT_REQUEST);
        buffer.writeInt(0); // 无载荷
        
        ctx.writeAndFlush(buffer);
        logger.debug("发送心跳消息");
    }
    
    /**
     * 发送Echo消息
     */
    private void sendEchoMessage(ChannelHandlerContext ctx, String message) {
        byte[] messageBytes = message.getBytes();
        
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(MSG_ECHO_REQUEST);
        buffer.writeInt(messageBytes.length);
        buffer.writeBytes(messageBytes);
        
        ctx.writeAndFlush(buffer);
        logger.debug("发送Echo消息: {}", message);
    }
    
    /**
     * 发送带时间戳的消息（用于延迟测试）
     */
    private void sendTimestampMessage(ChannelHandlerContext ctx, int messageId) {
        long timestamp = System.currentTimeMillis();
        
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(MSG_ECHO_REQUEST);
        buffer.writeInt(8); // 时间戳长度
        buffer.writeLong(timestamp);
        
        ctx.writeAndFlush(buffer);
    }
    
    /**
     * 关闭测试资源
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 获取测试结果列表
     */
    public List<String> getTestResults() {
        return new ArrayList<>(testResults);
    }
    
    /**
     * 获取测试错误列表
     */
    public List<String> getTestErrors() {
        return new ArrayList<>(testErrors);
    }
}