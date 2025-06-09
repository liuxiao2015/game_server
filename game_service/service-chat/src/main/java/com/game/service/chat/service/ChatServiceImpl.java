package com.game.service.chat.service;

import com.game.service.chat.model.ChatChannel;
import com.game.service.chat.model.ChatMessage;
import com.game.service.chat.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Chat service implementation
 * Provides comprehensive chat functionality including messaging, channels, and history
 *
 * @author lx
 * @date 2025/01/08
 */
@Service
public class ChatServiceImpl implements ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);
    
    @Autowired
    private ChatMessageRepository messageRepository;
    
    @Autowired
    private ChannelManager channelManager;
    
    @Autowired
    private MessageProcessor messageProcessor;
    
    @Autowired
    private OfflineMessageService offlineMessageService;

    @Override
    public String sendMessage(Long senderId, Long channelId, String content, String messageType) {
        try {
            // Validate channel access
            if (!channelManager.canSendMessage(senderId, channelId)) {
                logger.warn("User {} cannot send message to channel {}", senderId, channelId);
                return null;
            }
            
            // Process message content (sensitive word filtering, formatting, etc.)
            String processedContent = messageProcessor.processMessage(content);
            if (processedContent == null || processedContent.trim().isEmpty()) {
                logger.warn("Message content rejected after processing: {}", content);
                return null;
            }
            
            // Create chat message
            String messageId = UUID.randomUUID().toString();
            ChatMessage message = new ChatMessage(
                messageId, 
                senderId, 
                channelId, 
                ChatMessage.MessageType.valueOf(messageType.toUpperCase()), 
                processedContent
            );
            
            // Save message to ElasticSearch
            messageRepository.save(message);
            
            // Broadcast to channel members
            channelManager.broadcastToChannel(channelId, message);
            
            // Store for offline users
            offlineMessageService.storeOfflineMessageForChannel(channelId, message);
            
            logger.debug("Message sent successfully: {} to channel: {}", messageId, channelId);
            return messageId;
            
        } catch (Exception e) {
            logger.error("Error sending message from user {} to channel {}: {}", 
                    senderId, channelId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean joinChannel(Long userId, Long channelId) {
        try {
            boolean joined = channelManager.joinChannel(userId, channelId);
            if (joined) {
                logger.info("User {} joined channel {}", userId, channelId);
                
                // Send system message about user joining
                sendSystemMessage(channelId, "User " + userId + " joined the channel");
            }
            return joined;
            
        } catch (Exception e) {
            logger.error("Error joining channel {} for user {}: {}", channelId, userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean leaveChannel(Long userId, Long channelId) {
        try {
            boolean left = channelManager.leaveChannel(userId, channelId);
            if (left) {
                logger.info("User {} left channel {}", userId, channelId);
                
                // Send system message about user leaving
                sendSystemMessage(channelId, "User " + userId + " left the channel");
            }
            return left;
            
        } catch (Exception e) {
            logger.error("Error leaving channel {} for user {}: {}", channelId, userId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public ChatChannel createChannel(Long ownerId, ChatChannel.ChannelType type, String name) {
        try {
            ChatChannel channel = channelManager.createChannel(type, name, ownerId);
            logger.info("Created channel: {} (ID: {}) by user: {}", name, channel.getChannelId(), ownerId);
            return channel;
            
        } catch (Exception e) {
            logger.error("Error creating channel for user {}: {}", ownerId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<ChatMessage> getChatHistory(Long channelId, int offset, int limit) {
        try {
            Pageable pageable = PageRequest.of(offset / limit, limit);
            return messageRepository.findByChannelIdAndDeletedFalseOrderByTimestampDesc(channelId, pageable)
                    .getContent();
                    
        } catch (Exception e) {
            logger.error("Error getting chat history for channel {}: {}", channelId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<ChatMessage> searchMessages(Long channelId, String keyword, int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            
            if (channelId != null) {
                return messageRepository.findByChannelIdAndContentContainingAndDeletedFalseOrderByTimestampDesc(
                        channelId, keyword, pageable);
            } else {
                return messageRepository.findByContentContainingAndDeletedFalseOrderByTimestampDesc(
                        keyword, pageable);
            }
            
        } catch (Exception e) {
            logger.error("Error searching messages with keyword '{}': {}", keyword, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<ChatMessage> getOfflineMessages(Long userId) {
        try {
            return offlineMessageService.getOfflineMessages(userId);
            
        } catch (Exception e) {
            logger.error("Error getting offline messages for user {}: {}", userId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public void markMessagesAsRead(Long userId, Long channelId, String lastMessageId) {
        try {
            // This would typically update read status in a separate table/index
            // For now, just log the action
            logger.debug("Marked messages as read for user {} in channel {} up to message {}", 
                    userId, channelId, lastMessageId);
                    
        } catch (Exception e) {
            logger.error("Error marking messages as read for user {}: {}", userId, e.getMessage(), e);
        }
    }

    @Override
    public void blockUser(Long userId, Long targetUserId, int duration) {
        try {
            // This would typically store block information in a separate service/table
            logger.info("User {} blocked user {} for {} seconds", userId, targetUserId, duration);
            
        } catch (Exception e) {
            logger.error("Error blocking user {} by user {}: {}", targetUserId, userId, e.getMessage(), e);
        }
    }

    @Override
    public void reportUser(Long reporterId, Long targetUserId, String messageId, String reason) {
        try {
            // This would typically store report information for moderation
            logger.info("User {} reported user {} (message: {}) for: {}", 
                    reporterId, targetUserId, messageId, reason);
                    
        } catch (Exception e) {
            logger.error("Error reporting user {} by user {}: {}", targetUserId, reporterId, e.getMessage(), e);
        }
    }

    /**
     * Send system message to channel
     */
    private void sendSystemMessage(Long channelId, String content) {
        try {
            String messageId = UUID.randomUUID().toString();
            ChatMessage systemMessage = new ChatMessage(
                messageId, 
                0L, // System user ID
                channelId, 
                ChatMessage.MessageType.SYSTEM, 
                content
            );
            
            messageRepository.save(systemMessage);
            channelManager.broadcastToChannel(channelId, systemMessage);
            
        } catch (Exception e) {
            logger.error("Error sending system message to channel {}: {}", channelId, e.getMessage(), e);
        }
    }
}