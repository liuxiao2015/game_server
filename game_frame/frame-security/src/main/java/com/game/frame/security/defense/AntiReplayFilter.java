package com.game.frame.security.defense;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 防重放过滤器
 * @author lx
 * @date 2025/06/08
 */
@Component
public class AntiReplayFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AntiReplayFilter.class);
    
    private static final String NONCE_PREFIX = "security:nonce:";
    private static final String TIMESTAMP_HEADER = "X-Timestamp";
    private static final String NONCE_HEADER = "X-Nonce";
    private static final long REPLAY_WINDOW_MS = 300_000; // 5 minutes

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Check for replay attack
            if (isReplayAttack(request)) {
                logger.warn("Potential replay attack detected from IP: {}", getClientIpAddress(request));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Replay attack detected\"}");
                return;
            }
            
            // Store nonce for future checks
            storeNonce(request);
            
        } catch (Exception e) {
            logger.error("Error in anti-replay filter", e);
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 检查是否为重放攻击
     */
    private boolean isReplayAttack(HttpServletRequest request) {
        String timestamp = request.getHeader(TIMESTAMP_HEADER);
        String nonce = request.getHeader(NONCE_HEADER);
        
        // Skip check for requests without anti-replay headers
        if (timestamp == null || nonce == null) {
            return false;
        }
        
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            
            // Check timestamp window
            if (Math.abs(currentTime - requestTime) > REPLAY_WINDOW_MS) {
                logger.warn("Request timestamp outside replay window: {} vs {}", requestTime, currentTime);
                return true;
            }
            
            // Check if nonce already exists (replay)
            String nonceKey = NONCE_PREFIX + nonce;
            Boolean exists = redisTemplate.hasKey(nonceKey);
            
            return Boolean.TRUE.equals(exists);
            
        } catch (NumberFormatException e) {
            logger.warn("Invalid timestamp format: {}", timestamp);
            return true;
        }
    }

    /**
     * 存储nonce
     */
    private void storeNonce(HttpServletRequest request) {
        String nonce = request.getHeader(NONCE_HEADER);
        if (nonce != null) {
            try {
                String nonceKey = NONCE_PREFIX + nonce;
                // Store nonce with replay window expiration
                redisTemplate.opsForValue().set(nonceKey, "1", REPLAY_WINDOW_MS / 1000, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Failed to store nonce: {}", nonce, e);
            }
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip anti-replay for public endpoints
        return path.startsWith("/api/public/") ||
               path.startsWith("/health") ||
               path.startsWith("/actuator/") ||
               path.equals("/favicon.ico");
    }
}