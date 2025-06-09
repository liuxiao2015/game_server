package com.game.frame.security.utils;

import com.game.frame.security.auth.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 安全工具类
 * @author lx
 * @date 2025/06/08
 */
public class SecurityUtils {
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 获取当前用户
     */
    public static AuthUser getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
                return (AuthUser) authentication.getPrincipal();
            }
        } catch (Exception e) {
            // Log error or handle appropriately
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        AuthUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户名
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