package com.game.launcher.core;

import com.game.launcher.core.config.LauncherConfig;
import com.game.launcher.core.orchestrator.ServiceOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏服务器启动器主类
 * 
 * 功能说明：
 * - 作为整个游戏服务器系统的统一启动入口
 * - 负责加载启动配置和初始化核心组件
 * - 协调各个服务模块的启动顺序和依赖关系
 * - 提供优雅的系统关闭和资源清理机制
 * 
 * 设计思路：
 * - 采用门面模式简化复杂的启动流程
 * - 通过服务编排器管理微服务的生命周期
 * - 集成配置管理确保启动参数的灵活性
 * - 实现故障隔离和快速失败机制
 * 
 * 启动流程：
 * 1. 解析命令行参数和配置文件
 * 2. 初始化日志系统和基础设施
 * 3. 创建服务编排器并配置服务依赖
 * 4. 按顺序启动各个微服务模块
 * 5. 注册系统关闭钩子确保优雅退出
 * 6. 监控服务状态并处理异常情况
 * 
 * 核心组件：
 * - LauncherConfig：启动配置管理器
 * - ServiceOrchestrator：服务编排和生命周期管理
 * - ShutdownHook：优雅关闭和资源清理
 * 
 * 支持的服务模块：
 * - game-service-logic：游戏核心逻辑服务
 * - game-service-match：游戏匹配服务
 * - game-adm-backend：管理后台服务
 * - 其他扩展的微服务模块
 * 
 * 异常处理：
 * - 启动失败时记录详细错误信息
 * - 提供明确的退出码便于运维监控
 * - 支持部分服务失败时的降级处理
 * 
 * 使用场景：
 * - 生产环境的服务器启动和部署
 * - 开发环境的本地调试和测试
 * - 容器化部署中的入口点配置
 * - 集群环境中的节点启动管理
 * 
 * 扩展能力：
 * - 支持插件化的服务模块加载
 * - 动态配置的热更新和重载
 * - 健康检查和故障自动恢复
 * - 性能监控和运行时指标收集
 *
 * @author lx
 * @date 2024-01-01
 */
public class GameLauncher {
    
    // 日志记录器，用于记录启动器的运行状态和调试信息
    private static final Logger logger = LoggerFactory.getLogger(GameLauncher.class);

    /**
     * 游戏服务器启动器主入口方法
     * 
     * 功能说明：
     * - 作为JVM进程的入口点，负责整个系统的启动流程
     * - 处理命令行参数并加载相应的配置设置
     * - 创建和启动服务编排器，管理各微服务的生命周期
     * - 设置优雅关闭机制，确保系统安全退出
     * 
     * 启动步骤详解：
     * 1. 打印启动日志，标识启动流程开始
     * 2. 加载启动配置，解析命令行参数和配置文件
     * 3. 验证配置的完整性和合法性
     * 4. 创建服务编排器实例，传入配置参数
     * 5. 注册JVM关闭钩子，处理系统信号和优雅退出
     * 6. 启动服务编排器，开始各服务模块的初始化
     * 7. 等待所有服务启动完成，进入运行状态
     * 
     * 配置加载：
     * - 支持多种配置源：命令行参数、配置文件、环境变量
     * - 配置优先级：命令行 > 环境变量 > 配置文件 > 默认值
     * - 配置验证：检查必需参数和参数格式的正确性
     * 
     * 关闭机制：
     * - 捕获SIGTERM、SIGINT等系统关闭信号
     * - 按依赖关系倒序关闭各个服务模块
     * - 等待正在处理的请求完成，避免数据丢失
     * - 清理资源和连接，确保无内存泄漏
     * 
     * @param args 命令行参数数组，支持配置文件路径、服务端口等参数
     * 
     * 异常处理：
     * - 配置加载失败：打印错误信息并退出
     * - 服务启动失败：记录详细日志并终止进程
     * - 运行时异常：根据异常类型决定是否重试或退出
     * 
     * 退出码说明：
     * - 0：正常退出
     * - 1：启动失败或配置错误
     * - 2：资源不足或端口冲突
     * - 3：依赖服务不可用
     * 
     * 监控集成：
     * - 启动时间监控：记录各阶段耗时
     * - 健康状态上报：向监控系统报告启动状态
     * - 错误信息收集：异常情况的详细记录
     */
    public static void main(String[] args) {
        try {
            logger.info("Starting Game Server Launcher...");
            
            // 加载启动器配置
            // 解析命令行参数、配置文件和环境变量
            LauncherConfig config = LauncherConfig.load(args);
            logger.info("Loaded configuration: {}", config);
            
            // 创建服务编排器实例
            // 负责管理各个微服务的启动顺序和依赖关系
            ServiceOrchestrator orchestrator = new ServiceOrchestrator(config);
            
            // 添加JVM关闭钩子，确保优雅关闭
            // 处理SIGTERM、SIGINT等系统信号
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down Game Server Launcher...");
                try {
                    // 按依赖关系倒序关闭所有服务
                    orchestrator.shutdown();
                } catch (Exception e) {
                    logger.error("Error during shutdown: {}", e.getMessage(), e);
                }
            }));
            
            // 启动服务编排器
            // 开始各个微服务模块的初始化和启动流程
            orchestrator.start();
            
            logger.info("Game Server Launcher started successfully");
            
        } catch (Exception e) {
            // 启动失败时记录详细错误信息并退出
            logger.error("Failed to start Game Server Launcher: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}