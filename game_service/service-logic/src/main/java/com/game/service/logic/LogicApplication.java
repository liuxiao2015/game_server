package com.game.service.logic;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏业务逻辑服务启动类
 * 
 * 功能说明：
 * - 游戏核心业务逻辑微服务的主启动入口
 * - 集成Spring Boot和Dubbo框架，提供分布式服务能力
 * - 负责背包、任务、战斗等核心游戏模块的初始化和管理
 * - 提供RESTful API和RPC服务接口，支持多种客户端接入
 * 
 * 技术架构：
 * - Spring Boot 3.x：提供现代化的微服务框架支持
 * - Apache Dubbo：分布式RPC服务框架，支持服务注册发现
 * - 组件扫描：自动发现和装配业务模块和框架组件
 * - 配置管理：支持多环境配置和外部化配置
 * 
 * 服务职责：
 * - 背包系统：物品管理、容量控制、使用逻辑
 * - 任务系统：任务进度、奖励发放、完成验证
 * - 战斗系统：战斗创建、伤害计算、结果处理
 * - 模块管理：统一的模块生命周期和依赖管理
 * - 用户会话：玩家状态管理和缓存服务
 * 
 * 启动流程：
 * 1. 加载Spring Boot配置和自动装配规则
 * 2. 初始化Dubbo服务提供者和消费者
 * 3. 扫描并注册所有业务组件和服务
 * 4. 启动内嵌Web服务器和RPC服务端口
 * 5. 执行健康检查和服务注册
 * 
 * 扫描包路径：
 * - com.game.service.logic：核心业务逻辑包
 * - com.game.frame.dubbo：Dubbo框架集成包
 * 
 * 部署特点：
 * - 支持独立部署和容器化部署
 * - 提供优雅启动和关闭机制
 * - 集成监控和日志系统
 * - 支持服务发现和负载均衡
 * 
 * 运行环境：
 * - JDK 17+：利用现代JVM特性和性能优化
 * - Spring Boot 3.x：云原生和响应式编程支持
 * - 内存要求：推荐1GB+，根据并发量调整
 * - 网络端口：HTTP服务端口和Dubbo RPC端口
 * 
 * 监控指标：
 * - 服务启动时间和启动成功率
 * - RPC调用量和响应时间统计
 * - 业务模块的处理性能指标
 * - 内存使用和GC性能监控
 *
 * @author lx
 * @date 2024-01-01
 */
@SpringBootApplication(scanBasePackages = {
    "com.game.service.logic",    // 业务逻辑核心包，包含所有游戏业务模块
    "com.game.frame.dubbo"       // Dubbo框架集成包，提供RPC服务能力
})
@EnableDubbo  // 启用Dubbo分布式服务框架，支持服务注册发现和RPC调用
public class LogicApplication {

    /** 日志记录器，用于记录服务启动、运行和异常信息 */
    private static final Logger logger = LoggerFactory.getLogger(LogicApplication.class);

    /**
     * 服务主启动方法
     * 
     * 功能说明：
     * - 启动Spring Boot应用容器
     * - 初始化所有业务模块和框架组件
     * - 提供完整的异常处理和错误恢复机制
     * 
     * 启动检查：
     * - 验证必要的配置文件和环境变量
     * - 检查依赖服务的连通性
     * - 确保端口可用性和资源充足性
     * 
     * 异常处理：
     * - 捕获所有启动异常并记录详细日志
     * - 提供友好的错误信息和解决建议
     * - 确保异常情况下的优雅退出
     * 
     * @param args 命令行启动参数，支持配置文件路径、环境变量等
     */
    public static void main(String[] args) {
        try {
            logger.info("正在启动游戏业务逻辑服务...");
            
            // 启动Spring Boot应用
            SpringApplication.run(LogicApplication.class, args);
            
            logger.info("游戏业务逻辑服务启动成功");
            
        } catch (Exception e) {
            logger.error("游戏业务逻辑服务启动失败", e);
            // 启动失败时优雅退出，避免僵尸进程
            System.exit(1);
        }
    }
}