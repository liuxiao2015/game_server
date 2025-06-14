package com.game.service.chat.config;

import com.game.service.chat.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for chat service
 * Configures WebSocket endpoints and handlers
 *
 * @author lx
 * @date 2025/01/08
 */
@Configuration
@EnableWebSocket
/**
 * WebSocket配置类
 * 
 * 功能说明：
 * - 配置系统或模块的参数和属性
 * - 支持配置的自动加载和验证
 * - 集成Spring Boot配置管理机制
 *
 * @author lx
 * @date 2024-01-01
 */
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register chat WebSocket endpoint
        registry.addHandler(chatWebSocketHandler, "/chat")
                .setAllowedOrigins("*"); // In production, configure proper CORS
    }
}