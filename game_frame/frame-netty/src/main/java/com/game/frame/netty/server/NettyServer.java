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
 * Netty server startup and management with Epoll/NIO auto-selection,
 * Boss/Worker thread group configuration, Channel option optimization, and graceful shutdown
 *
 * @author lx
 * @date 2024-01-01
 */
public class NettyServer {
    
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    
    private final NettyServerConfig config;
    private final SessionManager sessionManager;
    private final MessageDispatcher messageDispatcher;
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    /**
     * Creates a new NettyServer
     * 
     * @param config server configuration
     * @param sessionManager session manager
     * @param messageDispatcher message dispatcher
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