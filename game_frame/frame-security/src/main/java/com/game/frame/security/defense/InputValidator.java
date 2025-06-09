package com.game.frame.security.defense;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 输入验证器
 * @author lx
 * @date 2025/06/08
 */
@Component
public class InputValidator {
    private static final Logger logger = LoggerFactory.getLogger(InputValidator.class);

    // SQL注入检测模式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i).*(union|select|insert|update|delete|drop|create|alter|exec|script|javascript|vbscript).*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // XSS攻击检测模式
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i).*(<script|</script|javascript:|vbscript:|onload=|onerror=|onclick=).*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // 路径遍历检测模式
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
            ".*(\\.\\.[\\\\/]|\\.\\.%2f|\\.\\.%5c).*",
            Pattern.CASE_INSENSITIVE);

    // 文件扩展名验证
    private static final Pattern SAFE_FILE_EXTENSION = Pattern.compile(
            ".*\\.(jpg|jpeg|png|gif|pdf|txt|doc|docx|xls|xlsx)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * SQL注入检测
     */
    public boolean checkSqlInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        boolean hasSqlInjection = SQL_INJECTION_PATTERN.matcher(input).matches();
        if (hasSqlInjection) {
            logger.warn("Potential SQL injection detected: {}", sanitizeForLog(input));
        }
        return hasSqlInjection;
    }

    /**
     * XSS攻击检测
     */
    public boolean checkXssAttack(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        boolean hasXss = XSS_PATTERN.matcher(input).matches();
        if (hasXss) {
            logger.warn("Potential XSS attack detected: {}", sanitizeForLog(input));
        }
        return hasXss;
    }

    /**
     * 路径遍历检测
     */
    public boolean checkPathTraversal(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        boolean hasPathTraversal = PATH_TRAVERSAL_PATTERN.matcher(path).matches();
        if (hasPathTraversal) {
            logger.warn("Potential path traversal detected: {}", sanitizeForLog(path));
        }
        return hasPathTraversal;
    }

    /**
     * HTML编码防XSS
     */
    public String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input);
    }

    /**
     * JavaScript编码
     */
    public String sanitizeJavaScript(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forJavaScript(input);
    }

    /**
     * URL编码
     */
    public String sanitizeUrl(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forUriComponent(input);
    }

    /**
     * XML编码
     */
    public String sanitizeXml(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forXml(input);
    }

    /**
     * 文件名安全检查
     */
    public boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }

        // 检查路径遍历
        if (checkPathTraversal(fileName)) {
            return false;
        }

        // 检查文件扩展名
        if (!SAFE_FILE_EXTENSION.matcher(fileName).matches()) {
            logger.warn("Unsafe file extension: {}", sanitizeForLog(fileName));
            return false;
        }

        return true;
    }

    /**
     * 验证用户名格式
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        // 长度检查
        if (username.length() < 3 || username.length() > 50) {
            return false;
        }

        // 字符检查：只允许字母、数字、下划线
        return username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * 验证邮箱格式
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * 验证密码强度
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // 至少包含一个大写字母、一个小写字母、一个数字、一个特殊字符
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * 验证IP地址格式
     */
    public boolean isValidIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
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
     * 综合安全检查
     */
    public boolean isSafeInput(String input) {
        if (input == null) {
            return true; // null is considered safe
        }

        // 检查各种攻击
        return !checkSqlInjection(input) && 
               !checkXssAttack(input) && 
               !checkPathTraversal(input);
    }

    /**
     * 清理输入以用于安全日志记录
     */
    private String sanitizeForLog(String input) {
        if (input == null) {
            return "null";
        }
        
        // 限制长度并转义特殊字符
        String sanitized = input.length() > 100 ? input.substring(0, 100) + "..." : input;
        return sanitized.replaceAll("[\r\n\t]", "_");
    }

    /**
     * 验证JSON格式
     */
    public boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            // Simple JSON validation
            json = json.trim();
            return (json.startsWith("{") && json.endsWith("}")) || 
                   (json.startsWith("[") && json.endsWith("]"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证数字范围
     */
    public boolean isValidNumberRange(String input, long min, long max) {
        try {
            long value = Long.parseLong(input);
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}