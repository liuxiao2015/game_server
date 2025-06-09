package com.game.launcher.core;

import com.game.launcher.core.config.LauncherConfig;
import com.game.launcher.core.orchestrator.ServiceOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Game launcher main class
 * Unified entry point for launching game services with orchestration support
 *
 * @author lx
 * @date 2025/01/08
 */
public class GameLauncher {
    
    private static final Logger logger = LoggerFactory.getLogger(GameLauncher.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting Game Server Launcher...");
            
            // Load launcher configuration
            LauncherConfig config = LauncherConfig.load(args);
            logger.info("Loaded configuration: {}", config);
            
            // Create and start service orchestrator
            ServiceOrchestrator orchestrator = new ServiceOrchestrator(config);
            
            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down Game Server Launcher...");
                try {
                    orchestrator.shutdown();
                } catch (Exception e) {
                    logger.error("Error during shutdown: {}", e.getMessage(), e);
                }
            }));
            
            // Start the orchestrator
            orchestrator.start();
            
            logger.info("Game Server Launcher started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start Game Server Launcher: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}