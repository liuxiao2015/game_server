package com.game.frame.security.defense;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * 安全编码器
 * 基于OWASP Java Encoder实现防注入编码
 * @author lx
 * @date 2025/06/08
 */
@Component
public class SecureEncoder {
    
    private static final Logger logger = LoggerFactory.getLogger(SecureEncoder.class);
    
    // SQL注入检测模式
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        Pattern.compile("(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)(or|and)\\s+\\d+\\s*=\\s*\\d+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)'\\s*(or|and)\\s*'", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)(--|#|/\\*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)(char|varchar|nchar|nvarchar)\\s*\\(", Pattern.CASE_INSENSITIVE)
    };
    
    // XSS攻击检测模式
    private static final Pattern[] XSS_PATTERNS = {
        Pattern.compile("(?i)<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("(?i)<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("(?i)<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("(?i)<embed[^>]*>.*?</embed>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("(?i)javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)onload\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)onerror\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?i)onclick\\s*=", Pattern.CASE_INSENSITIVE)
    };
    
    // 路径遍历检测模式
    private static final Pattern[] PATH_TRAVERSAL_PATTERNS = {
        Pattern.compile("(?i)\\.\\./"),
        Pattern.compile("(?i)\\.\\.\\\\"),
        Pattern.compile("(?i)%2e%2e%2f"),
        Pattern.compile("(?i)%2e%2e\\\\"),
        Pattern.compile("(?i)%2e%2e%5c")
    };
    
    /**
     * HTML编码 - 防止XSS攻击
     */
    public String encodeForHTML(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            return Encode.forHtml(input);
        } catch (Exception e) {
            logger.error("Failed to encode for HTML: {}", input, e);
            return escapeHtmlManually(input);
        }
    }
    
    /**
     * HTML属性编码
     */
    public String encodeForHTMLAttribute(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            return Encode.forHtmlAttribute(input);
        } catch (Exception e) {
            logger.error("Failed to encode for HTML attribute: {}", input, e);
            return escapeHtmlAttributeManually(input);
        }
    }
    
    /**
     * JavaScript编码
     */
    public String encodeForJavaScript(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            return Encode.forJavaScript(input);
        } catch (Exception e) {
            logger.error("Failed to encode for JavaScript: {}", input, e);
            return escapeJavaScriptManually(input);
        }
    }
    
    /**
     * CSS编码
     */
    public String encodeForCSS(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            return Encode.forCssString(input);
        } catch (Exception e) {
            logger.error("Failed to encode for CSS: {}", input, e);
            return escapeCSSManually(input);
        }
    }
    
    /**
     * URL编码
     */
    public String encodeForURL(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Failed to encode for URL: {}", input, e);
            return input;
        }
    }
    
    /**
     * URL解码
     */
    public String decodeFromURL(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            return URLDecoder.decode(input, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Failed to decode from URL: {}", input, e);
            return input;
        }
    }
    
    /**
     * XML编码
     */
    public String encodeForXML(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            return Encode.forXml(input);
        } catch (Exception e) {
            logger.error("Failed to encode for XML: {}", input, e);
            return escapeXmlManually(input);
        }
    }
    
    /**
     * 清理HTML标签
     */
    public String sanitizeHTML(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            // 移除所有HTML标签
            String cleaned = input.replaceAll("<[^>]+>", "");
            
            // 移除可能的脚本内容
            cleaned = cleaned.replaceAll("(?i)javascript:", "");
            cleaned = cleaned.replaceAll("(?i)vbscript:", "");
            cleaned = cleaned.replaceAll("(?i)data:", "");
            
            // HTML实体编码
            return encodeForHTML(cleaned);
            
        } catch (Exception e) {
            logger.error("Failed to sanitize HTML: {}", input, e);
            return encodeForHTML(input);
        }
    }
    
    /**
     * 检查SQL注入
     */
    public boolean containsSQLInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        try {
            String normalized = input.toLowerCase().trim();
            
            for (Pattern pattern : SQL_INJECTION_PATTERNS) {
                if (pattern.matcher(normalized).find()) {
                    logger.warn("SQL injection detected: {}", input);
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Failed to check SQL injection: {}", input, e);
            return true; // 发生错误时保守处理
        }
    }
    
    /**
     * 检查XSS攻击
     */
    public boolean containsXSS(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        try {
            String normalized = input.toLowerCase().trim();
            
            for (Pattern pattern : XSS_PATTERNS) {
                if (pattern.matcher(normalized).find()) {
                    logger.warn("XSS attack detected: {}", input);
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Failed to check XSS: {}", input, e);
            return true; // 发生错误时保守处理
        }
    }
    
    /**
     * 检查路径遍历
     */
    public boolean containsPathTraversal(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        try {
            String normalized = input.toLowerCase().trim();
            
            for (Pattern pattern : PATH_TRAVERSAL_PATTERNS) {
                if (pattern.matcher(normalized).find()) {
                    logger.warn("Path traversal detected: {}", input);
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Failed to check path traversal: {}", input, e);
            return true; // 发生错误时保守处理
        }
    }
    
    /**
     * 综合安全检查
     */
    public boolean isSafeInput(String input) {
        return !containsSQLInjection(input) && 
               !containsXSS(input) && 
               !containsPathTraversal(input);
    }
    
    /**
     * 安全清理输入
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            // 移除危险字符
            String cleaned = input;
            
            // 移除SQL注入相关字符
            cleaned = cleaned.replaceAll("(?i)(union|select|insert|update|delete|drop)", "");
            cleaned = cleaned.replaceAll("--", "");
            cleaned = cleaned.replaceAll("#", "");
            cleaned = cleaned.replaceAll("/\\*.*?\\*/", "");
            
            // 移除XSS相关字符
            cleaned = cleaned.replaceAll("(?i)<script[^>]*>.*?</script>", "");
            cleaned = cleaned.replaceAll("(?i)javascript:", "");
            cleaned = cleaned.replaceAll("(?i)vbscript:", "");
            
            // 移除路径遍历字符
            cleaned = cleaned.replaceAll("\\.\\.", "");
            cleaned = cleaned.replaceAll("%2e%2e", "");
            
            // HTML编码
            return encodeForHTML(cleaned);
            
        } catch (Exception e) {
            logger.error("Failed to sanitize input: {}", input, e);
            return encodeForHTML(input);
        }
    }
    
    // 手动转义方法（备用）
    private String escapeHtmlManually(String input) {
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
    
    private String escapeHtmlAttributeManually(String input) {
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;")
                   .replace(" ", "&#x20;");
    }
    
    private String escapeJavaScriptManually(String input) {
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("'", "\\'")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private String escapeCSSManually(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append("\\").append(Integer.toHexString(c));
            }
        }
        return sb.toString();
    }
    
    private String escapeXmlManually(String input) {
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}