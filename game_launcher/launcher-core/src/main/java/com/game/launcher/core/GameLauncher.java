package com.game.launcher.core;

import com.game.launcher.core.config.LauncherConfig;
import com.game.launcher.core.orchestrator.ServiceOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏服务器启动器
 * 
 * 功能说明：
 * - 负责游戏服务器集群的启动和协调管理
 * - 提供服务编排和依赖关系的管理功能
 * - 支持配置加载和服务生命周期控制
 * - 实现优雅启动和关闭的完整流程
 * 
 * 设计思路：
 * - 采用服务编排模式，统一管理多个服务的启动顺序
 * - 通过配置文件驱动，支持灵活的部署配置
 * - 集成健康检查和监控功能，确保服务可用性
 * - 提供优雅关闭机制，保证数据一致性和服务稳定性
 * 
 * 核心功能：
 * - 配置管理：加载和验证启动器配置文件
 * - 服务编排：按依赖关系启动各个游戏服务
 * - 生命周期管理：控制服务的启动、运行和关闭
 * - 异常处理：处理启动失败和运行异常
 * 
 * 启动流程：
 * 1. 加载启动器配置文件和命令行参数
 * 2. 创建服务编排器并配置服务依赖
 * 3. 注册JVM关闭钩子实现优雅关闭
 * 4. 启动服务编排器开始服务启动流程
 * 5. 监控服务状态和处理异常情况
 * 
 * 使用场景：
 * - 游戏服务器集群的统一启动入口
 * - 开发环境和生产环境的服务管理
 * - 微服务架构中的服务协调和编排
 * - 系统运维中的自动化部署和管理
 *
 * @author lx
 * @date 2024-01-01
 */
public class GameLauncher {
    
    // 日志记录器，用于记录启动器的运行状态和关键事件
    private static final Logger logger = LoggerFactory.getLogger(GameLauncher.class);

    /**
     * 启动器主入口方法
     * 
     * 功能说明：
     * - Java应用程序的主入口点，负责整个启动流程的协调
     * - 加载配置、创建服务编排器、启动服务集群
     * - 处理启动异常并提供优雅的错误处理机制
     * 
     * 启动步骤：
     * 1. 加载启动器配置文件和解析命令行参数
     * 2. 创建服务编排器实例并传入配置信息
     * 3. 注册JVM关闭钩子确保优雅关闭
     * 4. 启动服务编排器开始服务启动流程
     * 5. 记录启动成功日志或处理启动失败异常
     * 
     * @param args 命令行参数，用于传递配置文件路径和启动选项
     */
    public static void main(String[] args) {
        try {
            logger.info("开始启动游戏服务器...");
            
            // 加载启动器配置文件，支持从命令行参数指定配置路径
            LauncherConfig config = LauncherConfig.load(args);
            logger.info("配置加载完成: {}", config);
            
            // 创建服务编排器，负责管理所有服务的启动和运行
            ServiceOrchestrator orchestrator = new ServiceOrchestrator(config);
            
            // 注册JVM关闭钩子，确保程序终止时能够优雅地关闭所有服务
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("正在关闭游戏服务器...");
                try {
                    orchestrator.shutdown();
                } catch (Exception e) {
                    logger.error("关闭过程中发生错误: {}", e.getMessage(), e);
                }
            }));
            
            // 启动服务编排器，开始整个服务集群的启动流程
            orchestrator.start();
            
            logger.info("游戏服务器启动完成");
            
        } catch (Exception e) {
            logger.error("游戏服务器启动失败: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}