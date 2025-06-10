package com.game.service.chat;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

/**
 * Chat service application
 * Provides real-time chat functionality with WebSocket, RocketMQ, and ElasticSearch
 *
 * @author lx
 * @date 2025/01/08
 */
@SpringBootApplication
@EnableDubbo
@EnableWebSocket
/**
 * Chat应用启动类
 * 
 * 功能说明：
 * - 应用程序的主入口和启动配置
 * - 初始化Spring应用上下文
 * - 配置组件扫描和自动装配
 *
 * @author lx
 * @date 2024-01-01
 */
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}