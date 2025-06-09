package com.game.service.chat.service;

import com.game.service.chat.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Offline message service
 * Manages offline messages for users who are not online
 *
 * @author lx
 * @date 2025/01/08
 */
@Service
public class OfflineMessageService {
    
    private static final Logger logger = LoggerFactory.getLogger(OfflineMessageService.class);
    
    private static final String OFFLINE_MESSAGE_KEY_PREFIX = "offline_msg:";
    private static final String USER_LAST_ONLINE_KEY_PREFIX = "last_online:";
    private static final int OFFLINE_MESSAGE_EXPIRE_DAYS = 7; // Messages expire after 7 days
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Store offline message for users in a channel
     */
    public void storeOfflineMessageForChannel(Long channelId, ChatMessage message) {
        try {
            // Get channel members who are offline
            // This would typically integrate with the session manager to check online status
            // For now, we'll store for all potential recipients
            
            String messageKey = OFFLINE_MESSAGE_KEY_PREFIX + "channel:" + channelId;
            
            // Store message with expiration
            redisTemplate.opsForList().rightPush(messageKey, message);
            redisTemplate.expire(messageKey, OFFLINE_MESSAGE_EXPIRE_DAYS, TimeUnit.DAYS);
            
            logger.debug("Stored offline message for channel {}: {}", channelId, message.getMessageId());
            
        } catch (Exception e) {
            logger.error("Error storing offline message for channel {}: {}", channelId, e.getMessage(), e);
        }
    }

    /**
     * Store offline message for specific user
     */
    public void storeOfflineMessageForUser(Long userId, ChatMessage message) {
        try {
            String messageKey = OFFLINE_MESSAGE_KEY_PREFIX + "user:" + userId;
            
            // Store message with expiration
            redisTemplate.opsForList().rightPush(messageKey, message);
            redisTemplate.expire(messageKey, OFFLINE_MESSAGE_EXPIRE_DAYS, TimeUnit.DAYS);
            
            logger.debug("Stored offline message for user {}: {}", userId, message.getMessageId());
            
        } catch (Exception e) {
            logger.error("Error storing offline message for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Get offline messages for user
     */
    public List<ChatMessage> getOfflineMessages(Long userId) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            
            // Get messages from user-specific queue
            String userKey = OFFLINE_MESSAGE_KEY_PREFIX + "user:" + userId;
            List<Object> userMessages = redisTemplate.opsForList().range(userKey, 0, -1);
            if (userMessages != null) {
                for (Object msg : userMessages) {
                    if (msg instanceof ChatMessage) {
                        messages.add((ChatMessage) msg);
                    }
                }
            }
            
            // Get messages from channels user belongs to since last online
            LocalDateTime lastOnline = getLastOnlineTime(userId);
            if (lastOnline != null) {
                // Get channel messages since last online
                // This would integrate with ChannelManager to get user's channels
                messages.addAll(getChannelMessagesSince(userId, lastOnline));
            }
            
            // Sort by timestamp
            messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
            
            logger.debug("Retrieved {} offline messages for user {}", messages.size(), userId);
            return messages;
            
        } catch (Exception e) {
            logger.error("Error getting offline messages for user {}: {}", userId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Clear offline messages for user (call when user comes online)
     */
    public void clearOfflineMessages(Long userId) {
        try {
            String userKey = OFFLINE_MESSAGE_KEY_PREFIX + "user:" + userId;
            redisTemplate.delete(userKey);
            
            // Update last online time
            setLastOnlineTime(userId, LocalDateTime.now());
            
            logger.debug("Cleared offline messages for user {}", userId);
            
        } catch (Exception e) {
            logger.error("Error clearing offline messages for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Set last online time for user
     */
    public void setLastOnlineTime(Long userId, LocalDateTime time) {
        try {
            String key = USER_LAST_ONLINE_KEY_PREFIX + userId;
            redisTemplate.opsForValue().set(key, time, OFFLINE_MESSAGE_EXPIRE_DAYS, TimeUnit.DAYS);
            
        } catch (Exception e) {
            logger.error("Error setting last online time for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Get last online time for user
     */
    public LocalDateTime getLastOnlineTime(Long userId) {
        try {
            String key = USER_LAST_ONLINE_KEY_PREFIX + userId;
            Object time = redisTemplate.opsForValue().get(key);
            
            if (time instanceof LocalDateTime) {
                return (LocalDateTime) time;
            }
            
        } catch (Exception e) {
            logger.error("Error getting last online time for user {}: {}", userId, e.getMessage(), e);
        }
        
        // Default to 24 hours ago if no record
        return LocalDateTime.now().minusDays(1);
    }

    /**
     * Get channel messages since specified time
     */
    private List<ChatMessage> getChannelMessagesSince(Long userId, LocalDateTime since) {
        // This would integrate with ChannelManager and ChatMessageRepository
        // to get messages from user's channels since the specified time
        // For now, return empty list as placeholder
        return new ArrayList<>();
    }

    /**
     * Clean up expired offline messages
     */
    public void cleanupExpiredMessages() {
        try {
            // Get all offline message keys
            Set<String> keys = redisTemplate.keys(OFFLINE_MESSAGE_KEY_PREFIX + "*");
            
            if (keys != null && !keys.isEmpty()) {
                int cleanedCount = 0;
                for (String key : keys) {
                    // Redis TTL will handle expiration, but we can also manually clean up
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl != null && ttl <= 0) {
                        redisTemplate.delete(key);
                        cleanedCount++;
                    }
                }
                
                if (cleanedCount > 0) {
                    logger.info("Cleaned up {} expired offline message keys", cleanedCount);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error cleaning up expired offline messages: {}", e.getMessage(), e);
        }
    }

    /**
     * Get offline message count for user
     */
    public long getOfflineMessageCount(Long userId) {
        try {
            String userKey = OFFLINE_MESSAGE_KEY_PREFIX + "user:" + userId;
            Long count = redisTemplate.opsForList().size(userKey);
            return count != null ? count : 0;
            
        } catch (Exception e) {
            logger.error("Error getting offline message count for user {}: {}", userId, e.getMessage(), e);
            return 0;
        }
    }
}