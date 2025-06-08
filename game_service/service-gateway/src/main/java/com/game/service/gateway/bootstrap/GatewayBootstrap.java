package com.game.service.gateway.bootstrap;

import com.game.frame.netty.config.NettyServerConfig;
import com.game.frame.netty.handler.HandlerRegistry;
import com.game.frame.netty.handler.MessageDispatcher;
import com.game.frame.netty.server.NettyServer;
import com.game.frame.netty.session.SessionManager;
import com.game.service.gateway.config.GatewayConfig;
import com.game.service.gateway.handler.ClientMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

/**
 * Gateway startup bootstrap initializing Netty server and registering handlers
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
public class GatewayBootstrap implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayBootstrap.class);
    
    @Autowired
    private GatewayConfig gatewayConfig;
    
    @Autowired
    private ClientMessageHandler clientMessageHandler;
    
    private NettyServer nettyServer;
    private SessionManager sessionManager;
    private MessageDispatcher messageDispatcher;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing Gateway Bootstrap with config: {}", gatewayConfig);
        
        // Initialize components
        sessionManager = new SessionManager();
        HandlerRegistry handlerRegistry = new HandlerRegistry();
        messageDispatcher = new MessageDispatcher(handlerRegistry);
        
        // Register message handlers
        registerHandlers(handlerRegistry);
        
        // Configure Netty server
        NettyServerConfig serverConfig = new NettyServerConfig()
                .setPort(gatewayConfig.getPort())
                .setBossThreads(gatewayConfig.getBossThreads())
                .setWorkerThreads(gatewayConfig.getWorkerThreads())
                .setMaxConnections(gatewayConfig.getMaxConnections())
                .setConnectTimeoutMillis(gatewayConfig.getConnectTimeoutMs())
                .setHeartbeatInterval(gatewayConfig.getHeartbeatIntervalMs())
                .setHeartbeatTimeout(gatewayConfig.getHeartbeatTimeoutMs());
        
        // Create and start Netty server
        nettyServer = new NettyServer(serverConfig, sessionManager, messageDispatcher);
        nettyServer.start();
        
        logger.info("Gateway Bootstrap completed successfully");
        logger.info("Server statistics:\n{}", nettyServer.getServerStats());
    }
    
    /**
     * Registers message handlers
     * 
     * @param handlerRegistry handler registry
     */
    private void registerHandlers(HandlerRegistry handlerRegistry) {
        // Register a catch-all handler that handles all message types
        // In a real implementation, you would register specific handlers for specific message IDs
        messageDispatcher.registerHandler(new CatchAllHandler(clientMessageHandler));
        
        logger.info("Registered message handlers");
    }
    
    /**
     * Graceful shutdown
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Gateway Bootstrap...");
        
        if (nettyServer != null) {
            nettyServer.shutdown();
        }
        
        logger.info("Gateway Bootstrap shutdown completed");
    }
    
    /**
     * Wrapper handler that delegates to ClientMessageHandler
     */
    private static class CatchAllHandler implements com.game.frame.netty.handler.MessageHandler<com.game.frame.netty.protocol.MessageWrapper> {
        
        private final ClientMessageHandler delegate;
        
        public CatchAllHandler(ClientMessageHandler delegate) {
            this.delegate = delegate;
        }
        
        @Override
        public void handle(com.game.frame.netty.session.Session session, com.game.frame.netty.protocol.MessageWrapper message) {
            delegate.handle(session, message);
        }
        
        @Override
        public Class<com.game.frame.netty.protocol.MessageWrapper> getMessageType() {
            return com.game.frame.netty.protocol.MessageWrapper.class;
        }
        
        @Override
        public int getMessageId() {
            return -1; // Catch-all handler
        }
        
        @Override
        public boolean requiresAuthentication() {
            return false;
        }
    }
}