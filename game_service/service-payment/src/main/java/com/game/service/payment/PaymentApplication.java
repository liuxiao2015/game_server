package com.game.service.payment;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Payment service application
 * Provides payment processing with multiple channels and virtual currency support
 *
 * @author lx
 * @date 2025/01/08
 */
@SpringBootApplication
@EnableDubbo
@EnableAsync
@EnableScheduling
/**
 * Payment应用启动类
 * 
 * 功能说明：
 * - 应用程序的主入口和启动配置
 * - 初始化Spring应用上下文
 * - 配置组件扫描和自动装配
 *
 * @author lx
 * @date 2024-01-01
 */
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}