package com.game.frame.security.utils;

import com.game.frame.security.auth.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 安全工具类
 * 
 * 功能说明：
 * - 提供安全相关的通用工具方法
 * - 包含用户认证、权限检查、数据脱敏等功能
 * - 提供安全的随机数生成和字符串比较
 * - 支持IP地址验证和网络安全检查
 * 
 * 设计思路：
 * - 采用静态工具类设计，便于全局使用
 * - 集成Spring Security框架，获取当前用户信息
 * - 使用SecureRandom确保随机数的安全性
 * - 提供多种安全验证和防护机制
 * 
 * 安全特性：
 * - 防止时序攻击的字符串比较
 * - 敏感信息自动脱敏处理
 * - 安全的随机数和Token生成
 * - IP地址格式验证和内网检测
 * 
 * 使用场景：
 * - 用户认证和权限验证
 * - 敏感数据日志输出
 * - 随机密钥和Token生成
 * - 网络安全防护和检查
 * 
 * @author lx
 * @date 2025/06/08
 */
public class SecurityUtils {
    // 安全随机数生成器，用于生成高质量的随机数
    // 使用SecureRandom确保密码学安全性，适用于密钥、Token等敏感场景
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 获取当前登录用户信息
     * 
     * 实现逻辑：
     * 1. 从Spring Security上下文中获取认证信息
     * 2. 验证认证对象是否存在且类型正确
     * 3. 安全地转换为AuthUser对象
     * 4. 异常情况下返回null，避免系统崩溃
     * 
     * @return 当前登录用户信息，未登录或异常时返回null
     * 
     * 注意事项：
     * - 此方法依赖Spring Security上下文
     * - 在异步线程中可能无法获取到用户信息
     * - 建议在使用前进行null检查
     */
    public static AuthUser getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
                return (AuthUser) authentication.getPrincipal();
            }
        } catch (Exception e) {
            // 记录异常但不抛出，避免影响业务流程
            // 实际项目中应该使用日志框架记录此异常
        }
        return null;
    }

    /**
     * 获取当前登录用户的ID
     * 
     * @return 用户ID，未登录时返回null
     */
    public static Long getCurrentUserId() {
        AuthUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前登录用户的用户名
     * 
     * @return 用户名，未登录时返回null
     */
    public static String getCurrentUsername() {
        AuthUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 检查权限
     */
    public static boolean hasPermission(String permission) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                return authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals(permission));
            }
        } catch (Exception e) {
            // Log error or handle appropriately
        }
        return false;
    }

    /**
     * 检查角色
     */
    public static boolean hasRole(String role) {
        return hasPermission("ROLE_" + role);
    }

    /**
     * 生成安全随机数
     */
    public static String generateSecureRandom() {
        return generateSecureRandom(32);
    }

    /**
     * 生成指定长度的安全随机数
     */
    public static String generateSecureRandom(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    /**
     * 生成随机字符串（用于密钥、token等）
     */
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成nonce（用于防重放攻击）
     */
    public static String generateNonce() {
        return System.currentTimeMillis() + "_" + generateRandomString(16);
    }

    /**
     * 检查字符串是否为空或null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 安全的字符串比较（防止时序攻击）
     */
    public static boolean safeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * 掩码敏感信息（用于日志）
     */
    public static String maskSensitive(String sensitive) {
        if (isEmpty(sensitive)) {
            return sensitive;
        }
        
        if (sensitive.length() <= 4) {
            return "****";
        }
        
        int showLength = Math.min(2, sensitive.length() / 4);
        String start = sensitive.substring(0, showLength);
        String end = sensitive.substring(sensitive.length() - showLength);
        
        return start + "****" + end;
    }

    /**
     * 验证IP地址格式
     */
    public static boolean isValidIpAddress(String ip) {
        if (isEmpty(ip)) {
            return false;
        }
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 检查是否为内网IP
     */
    public static boolean isPrivateIp(String ip) {
        if (!isValidIpAddress(ip)) {
            return false;
        }
        
        String[] parts = ip.split("\\.");
        int first = Integer.parseInt(parts[0]);
        int second = Integer.parseInt(parts[1]);
        
        // 10.0.0.0/8
        if (first == 10) {
            return true;
        }
        
        // 172.16.0.0/12
        if (first == 172 && second >= 16 && second <= 31) {
            return true;
        }
        
        // 192.168.0.0/16
        if (first == 192 && second == 168) {
            return true;
        }
        
        // 127.0.0.0/8 (localhost)
        if (first == 127) {
            return true;
        }
        
        return false;
    }
}