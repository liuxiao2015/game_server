package com.game.frame.security.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * OAuth2配置
 * 支持第三方登录（Google、GitHub等）
 * @author lx
 * @date 2025/06/08
 */
@Configuration
public class OAuth2Config {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2Config.class);
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private TokenManager tokenManager;
    
    @Autowired
    private OAuth2TokenStore oAuth2TokenStore;
    
    /**
     * 配置OAuth2登录
     */
    public void configureOAuth2(HttpSecurity http) throws Exception {
        http.oauth2Login(oauth2 -> oauth2
            .successHandler(oauth2AuthenticationSuccessHandler())
            .failureHandler(oauth2AuthenticationFailureHandler())
        );
    }
    
    /**
     * OAuth2认证成功处理器
     */
    @Bean
    public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            try {
                // 获取用户信息（简化处理）
                String username = authentication.getName();
                String providerId = "oauth2_" + username;
                
                // 创建认证用户
                AuthUser authUser = new AuthUser(1L, username, UUID.randomUUID().toString());
                
                // 生成JWT Token
                String token = jwtTokenProvider.generateToken(authUser);
                String sessionId = UUID.randomUUID().toString();
                tokenManager.cacheToken(sessionId, token, jwtTokenProvider.getTokenExpiration());
                
                // 存储OAuth2信息（简化）
                oAuth2TokenStore.storeOAuth2UserInfo(String.valueOf(authUser.getUserId()), authentication.getName());
                
                // 重定向到前端应用
                String redirectUrl = request.getParameter("redirect_uri");
                if (redirectUrl == null) {
                    redirectUrl = "/"; // 默认重定向地址
                }
                
                // 将Token添加到重定向URL
                redirectUrl += "?token=" + token + "&sessionId=" + sessionId;
                
                response.sendRedirect(redirectUrl);
                
                logger.info("OAuth2 authentication successful for user: {}", authUser.getUsername());
                
            } catch (Exception e) {
                logger.error("OAuth2 authentication success handling failed", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication processing failed");
            }
        };
    }
    
    /**
     * OAuth2认证失败处理器
     */
    @Bean
    public AuthenticationFailureHandler oauth2AuthenticationFailureHandler() {
        return (request, response, exception) -> {
            logger.error("OAuth2 authentication failed", exception);
            
            String errorUrl = "/login?error=oauth2_failed&message=" + exception.getMessage();
            response.sendRedirect(errorUrl);
        };
    }
}