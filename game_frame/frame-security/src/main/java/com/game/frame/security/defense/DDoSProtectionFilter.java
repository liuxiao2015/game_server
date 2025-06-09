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
 * DDoS防护过滤器
 * 实现连接频率限制、黑名单机制、异常流量识别
 * @author lx
 * @date 2025/06/08
 */
@Component
public class DDoSProtectionFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(DDoSProtectionFilter.class);
    
    private static final String IP_BLACKLIST_PREFIX = "security:blacklist:ip:";
    private static final String IP_REQUEST_COUNT_PREFIX = "security:ddos:count:";
    private static final String IP_SUSPICIOUS_PREFIX = "security:ddos:suspicious:";
    
    // 配置参数
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;
    private static final int BLACKLIST_DURATION_MINUTES = 60;
    private static final int SUSPICIOUS_THRESHOLD = 50; // 可疑流量阈值
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestUri = request.getRequestURI();
        
        try {
            // 检查IP是否在黑名单中
            if (isIpBlacklisted(clientIp)) {
                logger.warn("Blocked request from blacklisted IP: {}", clientIp);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"Access denied\",\"reason\":\"IP blacklisted\"}");
                return;
            }
            
            // 检查请求频率
            if (!checkRequestFrequency(clientIp)) {
                logger.warn("Request frequency exceeded for IP: {}", clientIp);
                
                // 将IP添加到黑名单
                addToBlacklist(clientIp, "Excessive request frequency");
                
                response.setStatus(429); // 429 Too Many Requests
                response.getWriter().write("{\"error\":\"Too many requests\",\"reason\":\"Rate limit exceeded\"}");
                return;
            }
            
            // 检查异常行为
            if (isAbnormalBehavior(clientIp, userAgent, requestUri)) {
                logger.warn("Abnormal behavior detected for IP: {}", clientIp);
                markSuspicious(clientIp);
            }
            
            // 记录正常请求
            recordRequest(clientIp, userAgent, requestUri);
            
            // 继续处理请求
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("Error in DDoS protection filter for IP: {}", clientIp, e);
            filterChain.doFilter(request, response);
        }
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            // 取第一个IP
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }
        
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }
        
        String httpClientIp = request.getHeader("HTTP_CLIENT_IP");
        if (httpClientIp != null && !httpClientIp.isEmpty() && !"unknown".equalsIgnoreCase(httpClientIp)) {
            return httpClientIp;
        }
        
        String httpXForwardedFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (httpXForwardedFor != null && !httpXForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(httpXForwardedFor)) {
            return httpXForwardedFor;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 检查IP是否在黑名单中
     */
    private boolean isIpBlacklisted(String ip) {
        try {
            String key = IP_BLACKLIST_PREFIX + ip;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Failed to check IP blacklist for: {}", ip, e);
            return false;
        }
    }
    
    /**
     * 检查请求频率
     */
    private boolean checkRequestFrequency(String ip) {
        try {
            long currentTime = System.currentTimeMillis();
            long currentMinute = currentTime / 60000;
            long currentHour = currentTime / 3600000;
            
            String minuteKey = IP_REQUEST_COUNT_PREFIX + ip + ":minute:" + currentMinute;
            String hourKey = IP_REQUEST_COUNT_PREFIX + ip + ":hour:" + currentHour;
            
            // 检查每分钟请求数
            Long minuteCount = redisTemplate.opsForValue().increment(minuteKey);
            if (minuteCount == 1) {
                redisTemplate.expire(minuteKey, 60, TimeUnit.SECONDS);
            }
            
            if (minuteCount > MAX_REQUESTS_PER_MINUTE) {
                return false;
            }
            
            // 检查每小时请求数
            Long hourCount = redisTemplate.opsForValue().increment(hourKey);
            if (hourCount == 1) {
                redisTemplate.expire(hourKey, 3600, TimeUnit.SECONDS);
            }
            
            if (hourCount > MAX_REQUESTS_PER_HOUR) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to check request frequency for IP: {}", ip, e);
            return true; // 默认允许，避免误拦截
        }
    }
    
    /**
     * 检查异常行为
     */
    private boolean isAbnormalBehavior(String ip, String userAgent, String requestUri) {
        try {
            // 检查User-Agent
            if (isAbnormalUserAgent(userAgent)) {
                return true;
            }
            
            // 检查请求路径
            if (isAbnormalRequestUri(requestUri)) {
                return true;
            }
            
            // 检查请求模式
            if (isAbnormalRequestPattern(ip)) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Failed to check abnormal behavior for IP: {}", ip, e);
            return false;
        }
    }
    
    /**
     * 检查异常User-Agent
     */
    private boolean isAbnormalUserAgent(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return true; // 空User-Agent可疑
        }
        
        // 检查常见的爬虫或攻击工具User-Agent
        String ua = userAgent.toLowerCase();
        String[] suspiciousPatterns = {
            "bot", "crawler", "spider", "scraper", "python", "curl", "wget", 
            "scanner", "sqlmap", "nmap", "burp", "exploit"
        };
        
        for (String pattern : suspiciousPatterns) {
            if (ua.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查异常请求URI
     */
    private boolean isAbnormalRequestUri(String requestUri) {
        if (requestUri == null) {
            return false;
        }
        
        String uri = requestUri.toLowerCase();
        
        // 检查常见的攻击路径
        String[] attackPatterns = {
            "admin", "console", "manager", "phpmyadmin", "wp-admin",
            ".env", ".git", ".svn", "config", "backup",
            "sql", "inject", "xss", "script", "eval"
        };
        
        for (String pattern : attackPatterns) {
            if (uri.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查异常请求模式
     */
    private boolean isAbnormalRequestPattern(String ip) {
        try {
            // 检查短时间内的请求突增
            long currentTime = System.currentTimeMillis();
            long currentSecond = currentTime / 1000;
            
            String secondKey = IP_REQUEST_COUNT_PREFIX + ip + ":second:" + currentSecond;
            Long secondCount = redisTemplate.opsForValue().increment(secondKey);
            if (secondCount == 1) {
                redisTemplate.expire(secondKey, 1, TimeUnit.SECONDS);
            }
            
            // 如果1秒内请求超过10次，认为异常
            return secondCount > 10;
            
        } catch (Exception e) {
            logger.error("Failed to check request pattern for IP: {}", ip, e);
            return false;
        }
    }
    
    /**
     * 添加IP到黑名单
     */
    private void addToBlacklist(String ip, String reason) {
        try {
            String key = IP_BLACKLIST_PREFIX + ip;
            String value = reason + ":" + System.currentTimeMillis();
            
            redisTemplate.opsForValue().set(key, value, BLACKLIST_DURATION_MINUTES, TimeUnit.MINUTES);
            
            logger.warn("IP added to blacklist: {} reason: {}", ip, reason);
            
            // TODO: 发送告警通知
            
        } catch (Exception e) {
            logger.error("Failed to add IP to blacklist: {}", ip, e);
        }
    }
    
    /**
     * 标记IP为可疑
     */
    private void markSuspicious(String ip) {
        try {
            String key = IP_SUSPICIOUS_PREFIX + ip;
            Long suspiciousCount = redisTemplate.opsForValue().increment(key);
            if (suspiciousCount == 1) {
                redisTemplate.expire(key, 3600, TimeUnit.SECONDS); // 1小时过期
            }
            
            if (suspiciousCount >= SUSPICIOUS_THRESHOLD) {
                addToBlacklist(ip, "Suspicious behavior threshold exceeded");
            }
            
            logger.debug("IP marked as suspicious: {} count: {}", ip, suspiciousCount);
            
        } catch (Exception e) {
            logger.error("Failed to mark IP as suspicious: {}", ip, e);
        }
    }
    
    /**
     * 记录正常请求
     */
    private void recordRequest(String ip, String userAgent, String requestUri) {
        try {
            // 记录请求信息用于分析
            // 这里可以实现更复杂的请求分析逻辑
            logger.debug("Request recorded: IP={} UA={} URI={}", ip, userAgent, requestUri);
        } catch (Exception e) {
            logger.error("Failed to record request for IP: {}", ip, e);
        }
    }
    
    /**
     * 从黑名单中移除IP
     */
    public void removeFromBlacklist(String ip) {
        try {
            String key = IP_BLACKLIST_PREFIX + ip;
            redisTemplate.delete(key);
            logger.info("IP removed from blacklist: {}", ip);
        } catch (Exception e) {
            logger.error("Failed to remove IP from blacklist: {}", ip, e);
        }
    }
    
    /**
     * 获取黑名单信息
     */
    public String getBlacklistInfo(String ip) {
        try {
            String key = IP_BLACKLIST_PREFIX + ip;
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Failed to get blacklist info for IP: {}", ip, e);
            return null;
        }
    }
    
    /**
     * 清理过期数据
     */
    public void cleanupExpiredData() {
        try {
            // Redis会自动清理过期的键
            logger.debug("DDoS protection data cleanup completed");
        } catch (Exception e) {
            logger.error("Failed to cleanup DDoS protection data", e);
        }
    }
}