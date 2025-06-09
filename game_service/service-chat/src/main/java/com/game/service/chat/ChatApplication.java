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
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}