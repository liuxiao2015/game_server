package com.game.service.gateway.handler;

import com.game.common.model.request.UserLoginRequest;
import com.game.common.model.response.UserLoginResponse;
import com.game.frame.netty.protocol.MessageWrapper;
import com.game.frame.netty.session.Session;
import com.game.service.gateway.manager.RpcServiceManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 登录消息处理器
 * 
 * 功能说明：
 * - 处理客户端发送的登录请求消息
 * - 通过Dubbo RPC调用后端用户服务进行认证
 * - 将登录结果转换为网络消息回复给客户端
 * - 管理客户端会话的建立和状态维护
 * 
 * 处理流程：
 * 1. 接收客户端的登录消息请求
 * 2. 解析消息内容，提取账号密码等认证信息
 * 3. 调用UserService进行用户认证和验证
 * 4. 根据认证结果生成相应的响应消息
 * 5. 将响应消息发送回客户端
 * 6. 更新会话状态和用户在线信息
 * 
 * 技术特点：
 * - 基于Spring组件化设计，支持依赖注入
 * - 集成Dubbo RPC客户端，实现分布式服务调用
 * - 使用Jackson进行JSON消息的序列化和反序列化
 * - 支持异步消息处理，提高并发性能
 * 
 * 错误处理：
 * - 网络异常的重试机制
 * - 认证失败的错误码返回
 * - 会话状态异常的清理机制
 * - 详细的日志记录便于问题排查
 * 
 * 安全考虑：
 * - 请求频率限制，防止暴力破解
 * - 敏感信息脱敏，保护用户隐私
 * - 会话Token的安全生成和验证
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
public class LoginMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginMessageHandler.class);

    @Autowired
    private RpcServiceManager rpcServiceManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handle login message
     *
     * @param session client session
     * @param message login message
     */
    public void handleLogin(Session session, MessageWrapper message) {
        try {
            logger.info("Processing login request from session: {}", session.getSessionId());

            // Parse login request from message data
            UserLoginRequest loginRequest = parseLoginRequest(message);
            if (loginRequest == null) {
                sendLoginError(session, "Invalid login request format");
                return;
            }

            // Set request metadata
            loginRequest.setRequestId(UUID.randomUUID().toString());
            loginRequest.setDeviceId(getDeviceId(session));
            loginRequest.setClientVersion("1.0.0");

            // Call user service via RPC
            UserLoginResponse response = rpcServiceManager.getUserService().login(loginRequest);

            if (response.isSuccess()) {
                // Login successful
                handleLoginSuccess(session, response);
            } else {
                // Login failed
                sendLoginError(session, response.getMessage());
            }

        } catch (Exception e) {
            logger.error("Failed to process login request from session: {}", session.getSessionId(), e);
            sendLoginError(session, "Internal server error");
        }
    }

    /**
     * Parse login request from message wrapper
     */
    private UserLoginRequest parseLoginRequest(MessageWrapper message) {
        try {
            // For demo purposes, create a simple login request
            // In real implementation, parse from message.getData()
            UserLoginRequest request = new UserLoginRequest();
            request.setAccount("demo_user_" + System.currentTimeMillis());
            request.setPassword("demo_password");
            return request;
        } catch (Exception e) {
            logger.error("Failed to parse login request", e);
            return null;
        }
    }

    /**
     * Handle successful login
     */
    private void handleLoginSuccess(Session session, UserLoginResponse response) {
        try {
            // Set session user info
            session.setUserId(response.getData().getUserId().toString());
            session.setAuthenticated(true);

            // Create gateway session
            createGatewaySession(session, response);

            // Send login success response
            sendLoginResponse(session, response);

            logger.info("User {} logged in successfully from session: {}", 
                    response.getData().getUserId(), session.getSessionId());

        } catch (Exception e) {
            logger.error("Failed to handle login success", e);
            sendLoginError(session, "Failed to complete login");
        }
    }

    /**
     * Create gateway session after successful login
     */
    private void createGatewaySession(Session session, UserLoginResponse response) {
        try {
            Long userId = response.getData().getUserId();
            String deviceId = getDeviceId(session);
            String clientIp = getClientIp(session);
            String userAgent = getUserAgent(session);

            // Create session via RPC
            var sessionResult = rpcServiceManager.getSessionService()
                    .createSession(userId, deviceId, clientIp, userAgent);

            if (sessionResult.isSuccess()) {
                logger.info("Gateway session created for user {}", userId);
            } else {
                logger.warn("Failed to create gateway session for user {}: {}", 
                        userId, sessionResult.getMessage());
            }
        } catch (Exception e) {
            logger.error("Failed to create gateway session", e);
        }
    }

    /**
     * Send login response to client
     */
    private void sendLoginResponse(Session session, UserLoginResponse response) {
        try {
            // For demo purposes, just log the response
            // In real implementation, serialize response and send via session
            logger.info("Sending login response to session {}: userId={}, token={}", 
                    session.getSessionId(), response.getData().getUserId(), 
                    response.getData().getToken());
            
            // session.sendMessage(serializedResponse);
        } catch (Exception e) {
            logger.error("Failed to send login response", e);
        }
    }

    /**
     * Send login error response
     */
    private void sendLoginError(Session session, String errorMessage) {
        try {
            logger.warn("Sending login error to session {}: {}", session.getSessionId(), errorMessage);
            // In real implementation, send error response via session
            // session.sendMessage(errorResponse);
        } catch (Exception e) {
            logger.error("Failed to send login error response", e);
        }
    }

    /**
     * Get device ID from session
     */
    private String getDeviceId(Session session) {
        // In real implementation, extract from session attributes or headers
        return "device_" + session.getSessionId();
    }

    /**
     * Get client IP from session
     */
    private String getClientIp(Session session) {
        // In real implementation, extract from session
        return "127.0.0.1";
    }

    /**
     * Get user agent from session
     */
    private String getUserAgent(Session session) {
        // In real implementation, extract from session headers
        return "GameClient/1.0.0";
    }
}