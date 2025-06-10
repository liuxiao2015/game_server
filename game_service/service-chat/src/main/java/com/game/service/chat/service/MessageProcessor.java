package com.game.service.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Message processor for chat messages
 * Handles sensitive word filtering, content formatting, and validation
 *
 * @author lx
 * @date 2025/01/08
 */
@Component
/**
 * MessageProcessor
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
public class MessageProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
    
    // Sensitive words set (in production, this would be loaded from database/config)
    private final Set<String> sensitiveWords = ConcurrentHashMap.newKeySet();
    
    // Regular expressions for various content validation
    private static final Pattern EMOJI_PATTERN = Pattern.compile("[\\x{1F600}-\\x{1F64F}]|[\\x{1F300}-\\x{1F5FF}]|[\\x{1F680}-\\x{1F6FF}]|[\\x{1F1E0}-\\x{1F1FF}]");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");
    
    public MessageProcessor() {
        // Initialize with some common sensitive words (in production, load from configuration)
        initializeSensitiveWords();
    }

    /**
     * Process message content
     * Applies filtering, formatting, and validation
     */
    public String processMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        // Trim and normalize whitespace
        String processed = content.trim().replaceAll("\\s+", " ");
        
        // Check message length
        if (processed.length() > 1000) { // Max 1000 characters
            logger.warn("Message too long: {} characters", processed.length());
            return null;
        }
        
        // Apply sensitive word filtering
        processed = filterSensitiveWords(processed);
        
        // Process mentions (could expand to validate user existence)
        processed = processMentions(processed);
        
        // Process URLs (could add URL validation/shortening)
        processed = processUrls(processed);
        
        return processed;
    }

    /**
     * Filter sensitive words from content
     */
    private String filterSensitiveWords(String content) {
        String filtered = content;
        
        for (String sensitiveWord : sensitiveWords) {
            if (filtered.toLowerCase().contains(sensitiveWord.toLowerCase())) {
                // Replace with asterisks
                String replacement = "*".repeat(sensitiveWord.length());
                filtered = filtered.replaceAll("(?i)" + Pattern.quote(sensitiveWord), replacement);
                logger.debug("Filtered sensitive word: {}", sensitiveWord);
            }
        }
        
        return filtered;
    }

    /**
     * Process @mentions in messages
     */
    private String processMentions(String content) {
        // This could be expanded to validate mentioned users and add metadata
        return MENTION_PATTERN.matcher(content).replaceAll("@<user>$1</user>");
    }

    /**
     * Process URLs in messages
     */
    private String processUrls(String content) {
        // This could be expanded to validate URLs, create previews, or shorten URLs
        return URL_PATTERN.matcher(content).replaceAll("<url>$0</url>");
    }

    /**
     * Check if message contains only emojis
     */
    public boolean isEmojiOnly(String content) {
        String withoutEmojis = EMOJI_PATTERN.matcher(content).replaceAll("");
        return withoutEmojis.trim().isEmpty();
    }

    /**
     * Extract mentions from message
     */
    public Set<String> extractMentions(String content) {
        Set<String> mentions = ConcurrentHashMap.newKeySet();
        
        java.util.regex.Matcher matcher = MENTION_PATTERN.matcher(content);
        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }
        
        return mentions;
    }

    /**
     * Add sensitive word to filter list
     */
    public void addSensitiveWord(String word) {
        if (word != null && !word.trim().isEmpty()) {
            sensitiveWords.add(word.trim().toLowerCase());
            logger.info("Added sensitive word to filter: {}", word);
        }
    }

    /**
     * Remove sensitive word from filter list
     */
    public void removeSensitiveWord(String word) {
        if (word != null) {
            boolean removed = sensitiveWords.remove(word.trim().toLowerCase());
            if (removed) {
                logger.info("Removed sensitive word from filter: {}", word);
            }
        }
    }

    /**
     * Get count of sensitive words
     */
    public int getSensitiveWordCount() {
        return sensitiveWords.size();
    }

    /**
     * Initialize sensitive words (in production, load from database/config)
     */
    private void initializeSensitiveWords() {
        // Add some basic sensitive words for demonstration
        sensitiveWords.add("spam");
        sensitiveWords.add("scam");
        sensitiveWords.add("cheat");
        sensitiveWords.add("hack");
        sensitiveWords.add("bot");
        
        logger.info("Initialized {} sensitive words for filtering", sensitiveWords.size());
    }
}