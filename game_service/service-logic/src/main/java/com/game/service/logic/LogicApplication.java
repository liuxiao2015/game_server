package com.game.service.logic;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logic service startup class
 * Enables Dubbo and Spring Boot integration
 *
 * @author lx
 * @date 2024-01-01
 */
@SpringBootApplication(scanBasePackages = {
    "com.game.service.logic",
    "com.game.frame.dubbo"
})
@EnableDubbo
/**
 * Logic应用启动类
 * 
 * 功能说明：
 * - 应用程序的主入口和启动配置
 * - 初始化Spring应用上下文
 * - 配置组件扫描和自动装配
 *
 * @author lx
 * @date 2024-01-01
 */
public class LogicApplication {

    private static final Logger logger = LoggerFactory.getLogger(LogicApplication.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting Logic Service...");
            SpringApplication.run(LogicApplication.class, args);
            logger.info("Logic Service started successfully");
        } catch (Exception e) {
            logger.error("Failed to start Logic Service", e);
            System.exit(1);
        }
    }
}