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