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