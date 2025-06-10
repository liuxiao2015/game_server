package com.game.launcher.core;

import com.game.launcher.core.config.LauncherConfig;
import com.game.launcher.core.orchestrator.ServiceOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GameLauncher类
 * 
 * 功能说明：
 * - 提供核心业务功能的实现
 * - 封装相关的数据和操作方法
 * - 支持系统模块化和代码复用
 * 
 * 设计特点：
 * - 遵循面向对象设计原则
 * - 提供清晰的接口和实现分离
 * - 支持扩展和维护
 * 
 * 使用方式：
 * - 通过公共方法提供服务
 * - 支持依赖注入和配置管理
 * - 集成框架的生命周期管理
 *
 * @author lx
 * @date 2024-01-01
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