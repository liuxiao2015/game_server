package com.game.service.gateway.simple;

import com.game.common.Constants;
import com.game.frame.netty.config.NettyServerConfig;
import com.game.frame.netty.handler.HandlerRegistry;
import com.game.frame.netty.handler.MessageDispatcher;
import com.game.frame.netty.handler.MessageHandler;
import com.game.frame.netty.protocol.MessageWrapper;
import com.game.frame.netty.server.NettyServer;
import com.game.frame.netty.session.Session;
import com.game.frame.netty.session.SessionManager;

/**
 * Simple Gateway Server without Spring Boot for testing
 *
 * @author lx
 * @date 2024-01-01
 */
public class SimpleGatewayServer {
    
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Simple Gateway Server...");
        
        // Create components
        SessionManager sessionManager = new SessionManager();
        HandlerRegistry handlerRegistry = new HandlerRegistry();
        MessageDispatcher messageDispatcher = new MessageDispatcher(handlerRegistry);
        
        // Register a simple message handler
        MessageHandler<MessageWrapper> handler = new MessageHandler<MessageWrapper>() {
            @Override
            public void handle(Session session, MessageWrapper message) {
                System.out.printf("Received message ID %d from session %s%n", 
                        message.getMessageId(), session.getSessionId());
                
                // Simple echo response for heartbeat
                if (message.getMessageId() == Constants.MSG_HEARTBEAT_REQUEST) {
                    System.out.println("Processing heartbeat...");
                    session.updateActiveTime();
                }
            }
            
            @Override
            public Class<MessageWrapper> getMessageType() {
                return MessageWrapper.class;
            }
            
            @Override
            public int getMessageId() {
                return -1; // Catch all
            }
            
            @Override
            public boolean requiresAuthentication() {
                return false;
            }
        };
        
        handlerRegistry.registerHandler(handler);
        
        // Configure server
        NettyServerConfig config = new NettyServerConfig()
                .setPort(8888)
                .setBossThreads(1)
                .setWorkerThreads(4)
                .setMaxConnections(1000);
        
        // Create and start server
        NettyServer server = new NettyServer(config, sessionManager, messageDispatcher);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Gateway Server...");
            server.shutdown();
        }));
        
        try {
            server.start();
            System.out.println("Gateway Server started successfully on port 8888");
            System.out.println("Press Ctrl+C to stop the server");
            
            // Keep the server running
            server.awaitShutdown();
        } catch (Exception e) {
            System.err.println("Failed to start Gateway Server: " + e.getMessage());
            e.printStackTrace();
            server.shutdown();
        }
    }
}