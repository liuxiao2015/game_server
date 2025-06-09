package com.game.frame.security.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 密码验证器
 * 实现密码强度验证、历史密码检查、密码生成
 * @author lx
 * @date 2025/06/08
 */
@Component
public class PasswordValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordValidator.class);
    
    // 密码强度规则
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final int MIN_UPPERCASE = 1;
    private static final int MIN_LOWERCASE = 1;
    private static final int MIN_DIGITS = 1;
    private static final int MIN_SPECIAL_CHARS = 1;
    
    // 正则表达式模式
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");
    private static final Pattern SEQUENTIAL_PATTERN = Pattern.compile("(.)\\1{2,}"); // 连续相同字符
    
    // 常见弱密码
    private static final String[] COMMON_WEAK_PASSWORDS = {
        "123456", "password", "123456789", "12345678", "12345", "1234567", "1234567890",
        "qwerty", "abc123", "111111", "123123", "admin", "letmein", "welcome", "monkey",
        "dragon", "888888", "654321", "666666", "sunshine", "master", "shadow", "michael",
        "jennifer", "jordan", "hunter", "football", "superman", "harley", "ranger", "batman"
    };
    
    // 常见键盘模式
    private static final String[] KEYBOARD_PATTERNS = {
        "qwerty", "asdfgh", "zxcvbn", "123456", "qwertyuiop", "asdfghjkl", "zxcvbnm"
    };
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 验证密码强度
     */
    public PasswordValidationResult validatePassword(String password) {
        return validatePassword(password, null);
    }
    
    /**
     * 验证密码强度（包含用户信息）
     */
    public PasswordValidationResult validatePassword(String password, String username) {
        PasswordValidationResult result = new PasswordValidationResult();
        
        if (password == null || password.isEmpty()) {
            result.addError("密码不能为空");
            return result;
        }
        
        // 长度检查
        if (password.length() < MIN_LENGTH) {
            result.addError(String.format("密码长度至少需要 %d 位", MIN_LENGTH));
        }
        
        if (password.length() > MAX_LENGTH) {
            result.addError(String.format("密码长度不能超过 %d 位", MAX_LENGTH));
        }
        
        // 字符类型检查
        int uppercaseCount = countMatches(password, UPPERCASE_PATTERN);
        int lowercaseCount = countMatches(password, LOWERCASE_PATTERN);
        int digitCount = countMatches(password, DIGIT_PATTERN);
        int specialCharCount = countMatches(password, SPECIAL_CHAR_PATTERN);
        
        if (uppercaseCount < MIN_UPPERCASE) {
            result.addError(String.format("密码至少需要 %d 个大写字母", MIN_UPPERCASE));
        }
        
        if (lowercaseCount < MIN_LOWERCASE) {
            result.addError(String.format("密码至少需要 %d 个小写字母", MIN_LOWERCASE));
        }
        
        if (digitCount < MIN_DIGITS) {
            result.addError(String.format("密码至少需要 %d 个数字", MIN_DIGITS));
        }
        
        if (specialCharCount < MIN_SPECIAL_CHARS) {
            result.addError(String.format("密码至少需要 %d 个特殊字符", MIN_SPECIAL_CHARS));
        }
        
        // 弱密码检查
        if (isCommonWeakPassword(password)) {
            result.addError("密码过于简单，请使用更复杂的密码");
        }
        
        // 键盘模式检查
        if (containsKeyboardPattern(password)) {
            result.addError("密码不能包含连续的键盘字符");
        }
        
        // 连续字符检查
        if (containsSequentialChars(password)) {
            result.addError("密码不能包含3个或以上连续相同的字符");
        }
        
        // 用户名相关检查
        if (username != null && !username.isEmpty()) {
            if (password.toLowerCase().contains(username.toLowerCase())) {
                result.addError("密码不能包含用户名");
            }
        }
        
        // 计算密码强度分数
        int score = calculatePasswordScore(password, uppercaseCount, lowercaseCount, digitCount, specialCharCount);
        result.setScore(score);
        result.setStrength(getPasswordStrength(score));
        
        result.setValid(result.getErrors().isEmpty());
        
        logger.debug("Password validation result: score={}, strength={}, valid={}", 
                    score, result.getStrength(), result.isValid());
        
        return result;
    }
    
    /**
     * 检查密码历史
     */
    public boolean isPasswordInHistory(String password, List<String> passwordHistory, int maxHistorySize) {
        if (passwordHistory == null || passwordHistory.isEmpty()) {
            return false;
        }
        
        // 检查最近的历史密码
        int checkSize = Math.min(passwordHistory.size(), maxHistorySize);
        for (int i = 0; i < checkSize; i++) {
            String historicalPassword = passwordHistory.get(i);
            if (password.equals(historicalPassword)) {
                logger.warn("Password matches historical password");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 生成安全密码
     */
    public String generateSecurePassword() {
        return generateSecurePassword(12);
    }
    
    /**
     * 生成指定长度的安全密码
     */
    public String generateSecurePassword(int length) {
        if (length < MIN_LENGTH) {
            length = MIN_LENGTH;
        }
        
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        StringBuilder password = new StringBuilder();
        
        // 确保包含每种字符类型
        password.append(uppercase.charAt(secureRandom.nextInt(uppercase.length())));
        password.append(lowercase.charAt(secureRandom.nextInt(lowercase.length())));
        password.append(digits.charAt(secureRandom.nextInt(digits.length())));
        password.append(specialChars.charAt(secureRandom.nextInt(specialChars.length())));
        
        // 填充剩余长度
        String allChars = uppercase + lowercase + digits + specialChars;
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(secureRandom.nextInt(allChars.length())));
        }
        
        // 随机打乱字符顺序
        return shuffleString(password.toString());
    }
    
    /**
     * 生成密码建议
     */
    public List<String> generatePasswordSuggestions() {
        List<String> suggestions = new ArrayList<>();
        
        // 生成几个不同长度的密码建议
        suggestions.add(generateSecurePassword(8));
        suggestions.add(generateSecurePassword(12));
        suggestions.add(generateSecurePassword(16));
        
        return suggestions;
    }
    
    /**
     * 计算密码破解时间估计
     */
    public String estimateCrackTime(String password) {
        if (password == null || password.isEmpty()) {
            return "立即";
        }
        
        // 计算字符集大小
        int charsetSize = 0;
        if (UPPERCASE_PATTERN.matcher(password).find()) charsetSize += 26;
        if (LOWERCASE_PATTERN.matcher(password).find()) charsetSize += 26;
        if (DIGIT_PATTERN.matcher(password).find()) charsetSize += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) charsetSize += 32;
        
        // 计算可能的组合数
        double combinations = Math.pow(charsetSize, password.length());
        
        // 假设每秒尝试10^9次（现代GPU）
        double attemptsPerSecond = 1e9;
        double averageTime = combinations / (2 * attemptsPerSecond);
        
        return formatCrackTime(averageTime);
    }
    
    /**
     * 格式化破解时间
     */
    private String formatCrackTime(double seconds) {
        if (seconds < 1) {
            return "立即";
        } else if (seconds < 60) {
            return String.format("%.0f 秒", seconds);
        } else if (seconds < 3600) {
            return String.format("%.0f 分钟", seconds / 60);
        } else if (seconds < 86400) {
            return String.format("%.0f 小时", seconds / 3600);
        } else if (seconds < 31536000) {
            return String.format("%.0f 天", seconds / 86400);
        } else {
            return String.format("%.0f 年", seconds / 31536000);
        }
    }
    
    /**
     * 统计正则表达式匹配数量
     */
    private int countMatches(String text, Pattern pattern) {
        return (int) pattern.matcher(text).results().count();
    }
    
    /**
     * 检查是否为常见弱密码
     */
    private boolean isCommonWeakPassword(String password) {
        String lowerPassword = password.toLowerCase();
        for (String weakPassword : COMMON_WEAK_PASSWORDS) {
            if (lowerPassword.equals(weakPassword) || lowerPassword.contains(weakPassword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否包含键盘模式
     */
    private boolean containsKeyboardPattern(String password) {
        String lowerPassword = password.toLowerCase();
        for (String pattern : KEYBOARD_PATTERNS) {
            if (lowerPassword.contains(pattern) || lowerPassword.contains(reverse(pattern))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否包含连续相同字符
     */
    private boolean containsSequentialChars(String password) {
        return SEQUENTIAL_PATTERN.matcher(password).find();
    }
    
    /**
     * 计算密码强度分数
     */
    private int calculatePasswordScore(String password, int uppercase, int lowercase, int digits, int specialChars) {
        int score = 0;
        
        // 长度分数
        score += password.length() * 2;
        
        // 字符类型分数
        score += uppercase * 2;
        score += lowercase * 2;
        score += digits * 2;
        score += specialChars * 3;
        
        // 复杂度奖励
        int charTypes = 0;
        if (uppercase > 0) charTypes++;
        if (lowercase > 0) charTypes++;
        if (digits > 0) charTypes++;
        if (specialChars > 0) charTypes++;
        
        if (charTypes >= 3) score += 10;
        if (charTypes >= 4) score += 10;
        
        // 长度奖励
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 10;
        
        // 惩罚项
        if (isCommonWeakPassword(password)) score -= 20;
        if (containsKeyboardPattern(password)) score -= 10;
        if (containsSequentialChars(password)) score -= 10;
        
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * 根据分数获取密码强度等级
     */
    private PasswordStrength getPasswordStrength(int score) {
        if (score < 30) {
            return PasswordStrength.VERY_WEAK;
        } else if (score < 50) {
            return PasswordStrength.WEAK;
        } else if (score < 70) {
            return PasswordStrength.FAIR;
        } else if (score < 85) {
            return PasswordStrength.GOOD;
        } else {
            return PasswordStrength.STRONG;
        }
    }
    
    /**
     * 反转字符串
     */
    private String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }
    
    /**
     * 随机打乱字符串
     */
    private String shuffleString(String str) {
        char[] chars = str.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
    
    /**
     * 密码强度枚举
     */
    public enum PasswordStrength {
        VERY_WEAK("非常弱", "#ff0000"),
        WEAK("弱", "#ff6600"),
        FAIR("一般", "#ffcc00"),
        GOOD("好", "#66cc00"),
        STRONG("强", "#00cc00");
        
        private final String description;
        private final String color;
        
        PasswordStrength(String description, String color) {
            this.description = description;
            this.color = color;
        }
        
        public String getDescription() { return description; }
        public String getColor() { return color; }
    }
    
    /**
     * 密码验证结果类
     */
    public static class PasswordValidationResult {
        private boolean valid;
        private int score;
        private PasswordStrength strength;
        private List<String> errors = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        
        public PasswordStrength getStrength() { return strength; }
        public void setStrength(PasswordStrength strength) { this.strength = strength; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        @Override
        public String toString() {
            return String.format("PasswordValidationResult{valid=%s, score=%d, strength=%s, errors=%s}",
                    valid, score, strength, errors);
        }
    }
}