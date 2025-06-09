package com.game.service.gateway;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gateway service startup class with Spring Boot integration, 
 * Netty server startup, and configuration loading
 *
 * @author lx
 * @date 2024-01-01
 */
@SpringBootApplication(scanBasePackages = {
    "com.game.service.gateway",
    "com.game.frame.dubbo"
})
@EnableDubbo
/**
 * Gateway应用启动类
 * 
 * 功能说明：
 * - 应用程序的主入口类
 * - 配置Spring Boot应用的启动参数
 * - 初始化应用上下文和核心组件
 * - 管理应用的生命周期
 * 
 * 启动流程：
 * 1. 加载配置文件和环境变量
 * 2. 初始化Spring应用上下文
 * 3. 启动内置的Web服务器（如有）
 * 4. 执行应用初始化回调
 * 5. 开始接收和处理请求
 * 
 * 注解说明：
 * - @SpringBootApplication：启用自动配置和组件扫描
 * - @EnableXxx：开启特定功能模块
 * - @ComponentScan：指定组件扫描路径
 *
 * @author lx
 * @date 2024-01-01
 */
public class GatewayApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(GatewayApplication.class);
    
    public static void main(String[] args) {
        try {
            logger.info("Starting Gateway Service...");
            SpringApplication.run(GatewayApplication.class, args);
            logger.info("Gateway Service started successfully");
        } catch (Exception e) {
            logger.error("Failed to start Gateway Service", e);
            System.exit(1);
        }
    }
}