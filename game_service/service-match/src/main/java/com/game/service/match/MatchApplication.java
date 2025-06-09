package com.game.service.match;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Match service application
 * Provides game matching functionality with ELO ranking and algorithm support
 *
 * @author lx
 * @date 2025/01/08
 */
@SpringBootApplication
@EnableDubbo
@EnableAsync
@EnableScheduling
/**
 * Match应用启动类
 * 
 * 功能说明：
 * - 应用程序的主入口和启动配置
 * - 初始化Spring应用上下文
 * - 配置组件扫描和自动装配
 *
 * @author lx
 * @date 2024-01-01
 */
public class MatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(MatchApplication.class, args);
    }
}