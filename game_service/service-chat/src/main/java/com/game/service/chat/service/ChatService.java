package com.game.service.chat.service;

import com.game.service.chat.model.ChatMessage;
import com.game.service.chat.model.ChatChannel;

import java.util.List;

/**
 * Chat service interface
 * Defines core chat functionality including messaging, channels, and history
 *
 * @author lx
 * @date 2025/01/08
 */
/**
 * ChatService
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
public interface ChatService {

    /**
     * Send a chat message
     * 
     * @param senderId sender user ID
     * @param channelId target channel ID
     * @param content message content
     * @param messageType message type (TEXT, IMAGE, etc.)
     * @return message ID if successful
     */
    String sendMessage(Long senderId, Long channelId, String content, String messageType);

    /**
     * Join a chat channel
     * 
     * @param userId user ID
     * @param channelId channel ID
     * @return true if successfully joined
     */
    boolean joinChannel(Long userId, Long channelId);

    /**
     * Leave a chat channel
     * 
     * @param userId user ID
     * @param channelId channel ID
     * @return true if successfully left
     */
    boolean leaveChannel(Long userId, Long channelId);

    /**
     * Create a new chat channel
     * 
     * @param ownerId owner user ID
     * @param type channel type
     * @param name channel name
     * @return created channel
     */
    ChatChannel createChannel(Long ownerId, ChatChannel.ChannelType type, String name);

    /**
     * Get chat history for a channel
     * 
     * @param channelId channel ID
     * @param offset pagination offset
     * @param limit number of messages to retrieve
     * @return list of chat messages
     */
    List<ChatMessage> getChatHistory(Long channelId, int offset, int limit);

    /**
     * Search chat messages
     * 
     * @param channelId channel ID (optional, null for all channels)
     * @param keyword search keyword
     * @param limit maximum results
     * @return list of matching messages
     */
    List<ChatMessage> searchMessages(Long channelId, String keyword, int limit);

    /**
     * Get offline messages for user
     * 
     * @param userId user ID
     * @return list of offline messages
     */
    List<ChatMessage> getOfflineMessages(Long userId);

    /**
     * Mark messages as read
     * 
     * @param userId user ID
     * @param channelId channel ID
     * @param lastMessageId last read message ID
     */
    void markMessagesAsRead(Long userId, Long channelId, String lastMessageId);

    /**
     * Block/mute a user
     * 
     * @param userId user ID who wants to block
     * @param targetUserId user ID to be blocked
     * @param duration block duration in seconds (0 for permanent)
     */
    void blockUser(Long userId, Long targetUserId, int duration);

    /**
     * Report a message or user
     * 
     * @param reporterId reporter user ID
     * @param targetUserId target user ID (optional)
     * @param messageId message ID (optional)
     * @param reason report reason
     */
    void reportUser(Long reporterId, Long targetUserId, String messageId, String reason);
}