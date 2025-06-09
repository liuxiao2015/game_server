package com.game.frame.netty.server;

import com.game.frame.netty.config.NettyServerConfig;
import com.game.frame.netty.handler.MessageDispatcher;
import com.game.frame.netty.session.SessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty服务器启动和管理类
 * 
 * 功能说明：
 * - 自动选择Epoll/NIO传输模式以获得最佳性能
 * - 配置Boss/Worker线程组，优化网络I/O处理
 * - 提供Channel选项优化和连接管理
 * - 支持优雅启动和关闭机制
 * 
 * 设计思路：
 * - 根据操作系统自动选择最优的网络传输实现
 * - 分离连接接受(Boss)和I/O处理(Worker)线程池
 * - 集成会话管理和消息分发机制
 * - 提供完整的生命周期管理
 * 
 * 性能优化：
 * - Linux系统下优先使用Epoll，提升并发性能
 * - 合理配置线程池大小，避免资源浪费
 * - 优化TCP选项，提升网络传输效率
 * - 实现连接数限制和流量控制
 * 
 * 使用场景：
 * - 游戏网关服务器，处理客户端连接
 * - 内部服务通信，提供RPC网络支持
 * - 实时通信服务，如聊天、匹配等
 * - 高并发长连接服务
 * 
 * @author lx
 * @date 2024-01-01
 */
public class NettyServer {
    
    // 日志记录器，用于记录服务器启动、运行和异常信息
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    
    // 服务器配置信息，包含端口、线程数、超时等参数
    private final NettyServerConfig config;
    // 会话管理器，负责客户端连接的生命周期管理
    private final SessionManager sessionManager;
    // 消息分发器，处理入站消息的路由和分发
    private final MessageDispatcher messageDispatcher;
    // 服务器启动状态标志，使用原子操作确保线程安全
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    // Boss线程组，专门处理新连接的接受
    private EventLoopGroup bossGroup;
    // Worker线程组，处理已建立连接的I/O操作
    private EventLoopGroup workerGroup;
    // 服务器Channel，代表监听端口的服务器套接字
    private Channel serverChannel;
    
    /**
     * 创建Netty服务器实例
     * 
     * @param config 服务器配置对象，包含端口、线程数等参数
     * @param sessionManager 会话管理器，负责连接生命周期管理
     * @param messageDispatcher 消息分发器，处理业务消息路由
     */
    public NettyServer(NettyServerConfig config, SessionManager sessionManager, MessageDispatcher messageDispatcher) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.messageDispatcher = messageDispatcher;
    }
    
    /**
     * Starts the Netty server
     * 
     * @throws Exception if startup fails
     */
    public void start() throws Exception {
        if (!started.compareAndSet(false, true)) {
            logger.warn("NettyServer is already started");
            return;
        }
        
        logger.info("Starting NettyServer with config: {}", config);
        
        // Choose optimal transport implementation
        boolean useEpoll = Epoll.isAvailable();
        logger.info("Using {} transport", useEpoll ? "Epoll" : "NIO");
        
        // Create event loop groups
        if (useEpoll) {
            bossGroup = new EpollEventLoopGroup(config.getBossThreads(), 
                    new DefaultThreadFactory("NettyBoss", true));
            workerGroup = new EpollEventLoopGroup(config.getWorkerThreads(), 
                    new DefaultThreadFactory("NettyWorker", true));
        } else {
            bossGroup = new NioEventLoopGroup(config.getBossThreads(), 
                    new DefaultThreadFactory("NettyBoss", true));
            workerGroup = new NioEventLoopGroup(config.getWorkerThreads(), 
                    new DefaultThreadFactory("NettyWorker", true));
        }
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(new ServerChannelInitializer(sessionManager, messageDispatcher, config))
                    .option(ChannelOption.SO_BACKLOG, config.getSoBacklog())
                    .option(ChannelOption.SO_REUSEADDR, config.isSoReuseAddr())
                    .childOption(ChannelOption.TCP_NODELAY, config.isTcpNoDelay())
                    .childOption(ChannelOption.SO_KEEPALIVE, config.isSoKeepAlive())
                    .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis())
                    .childOption(ChannelOption.SO_SNDBUF, config.getSendBufferSize())
                    .childOption(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize())
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT);
            
            // Bind and start to accept incoming connections
            ChannelFuture future = bootstrap.bind(config.getPort()).sync();
            serverChannel = future.channel();
            
            logger.info("NettyServer started successfully on port {}", config.getPort());
            
        } catch (Exception e) {
            logger.error("Failed to start NettyServer", e);
            shutdown();
            throw e;
        }
    }
    
    /**
     * Stops the Netty server gracefully
     */
    public void shutdown() {
        if (!started.compareAndSet(true, false)) {
            logger.warn("NettyServer is not started");
            return;
        }
        
        logger.info("Shutting down NettyServer...");
        
        try {
            // Close server channel
            if (serverChannel != null) {
                serverChannel.close().sync();
                logger.info("Server channel closed");
            }
            
            // Clear all sessions
            sessionManager.clear();
            
            // Shutdown message dispatcher
            messageDispatcher.shutdown();
            
        } catch (Exception e) {
            logger.error("Error during server shutdown", e);
        } finally {
            // Shutdown event loop groups
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            
            logger.info("NettyServer shutdown completed");
        }
    }
    
    /**
     * Checks if the server is running
     * 
     * @return true if running
     */
    public boolean isRunning() {
        return started.get() && serverChannel != null && serverChannel.isActive();
    }
    
    /**
     * Waits for the server to shut down
     * 
     * @throws InterruptedException if interrupted
     */
    public void awaitShutdown() throws InterruptedException {
        if (serverChannel != null) {
            serverChannel.closeFuture().sync();
        }
    }
    
    /**
     * Gets server statistics
     * 
     * @return server stats string
     */
    public String getServerStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("NettyServer Status:\n");
        stats.append("  Running: ").append(isRunning()).append("\n");
        stats.append("  Port: ").append(config.getPort()).append("\n");
        stats.append("  Boss Threads: ").append(config.getBossThreads()).append("\n");
        stats.append("  Worker Threads: ").append(config.getWorkerThreads()).append("\n");
        stats.append("  Active Sessions: ").append(sessionManager.getSessionCount()).append("\n");
        stats.append("  Authenticated Sessions: ").append(sessionManager.getAuthenticatedSessionCount()).append("\n");
        stats.append(messageDispatcher.getHandlerInfo());
        
        return stats.toString();
    }
}