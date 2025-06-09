package com.game.service.chat.service;

import com.game.service.chat.model.ChatChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Chat channel manager
 * Manages chat channels and their members
 *
 * @author lx
 * @date 2025/01/08
 */
@Component
public class ChannelManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ChannelManager.class);
    
    // Channel storage
    private final ConcurrentHashMap<Long, ChatChannel> channels = new ConcurrentHashMap<>();
    
    // Channel ID generator
    private final AtomicLong channelIdGenerator = new AtomicLong(1000L);
    
    // User to channels mapping
    private final ConcurrentHashMap<Long, ConcurrentHashMap<Long, Boolean>> userChannels = new ConcurrentHashMap<>();

    /**
     * Create a new channel
     */
    public ChatChannel createChannel(ChatChannel.ChannelType type, String name, Long ownerId) {
        Long channelId = channelIdGenerator.incrementAndGet();
        ChatChannel channel = new ChatChannel(channelId, type, name, ownerId);
        
        // Add owner as member
        channel.addMember(ownerId);
        
        channels.put(channelId, channel);
        addUserToChannel(ownerId, channelId);
        
        logger.info("Created channel: {} (ID: {}) by user: {}", name, channelId, ownerId);
        return channel;
    }

    /**
     * Get channel by ID
     */
    public ChatChannel getChannel(Long channelId) {
        return channels.get(channelId);
    }

    /**
     * Join channel
     */
    public boolean joinChannel(Long userId, Long channelId) {
        ChatChannel channel = channels.get(channelId);
        if (channel == null || !channel.isActive()) {
            return false;
        }
        
        // Check if already a member
        if (channel.isMember(userId)) {
            return true;
        }
        
        // Add member to channel
        boolean added = channel.addMember(userId);
        if (added) {
            addUserToChannel(userId, channelId);
            logger.info("User {} joined channel: {}", userId, channelId);
        }
        
        return added;
    }

    /**
     * Leave channel
     */
    public boolean leaveChannel(Long userId, Long channelId) {
        ChatChannel channel = channels.get(channelId);
        if (channel == null) {
            return false;
        }
        
        boolean removed = channel.removeMember(userId);
        if (removed) {
            removeUserFromChannel(userId, channelId);
            logger.info("User {} left channel: {}", userId, channelId);
            
            // If channel is empty and not a system channel, deactivate it
            if (channel.getMemberCount() == 0 && 
                channel.getType() != ChatChannel.ChannelType.WORLD &&
                channel.getType() != ChatChannel.ChannelType.SYSTEM) {
                channel.setActive(false);
                logger.info("Deactivated empty channel: {}", channelId);
            }
        }
        
        return removed;
    }

    /**
     * Check if user can send messages to channel
     */
    public boolean canSendMessage(Long userId, Long channelId) {
        ChatChannel channel = channels.get(channelId);
        if (channel == null || !channel.isActive()) {
            return false;
        }
        
        // Check if user is a member
        return channel.isMember(userId);
    }

    /**
     * Get all channels for a user
     */
    public ConcurrentHashMap<Long, Boolean> getUserChannels(Long userId) {
        return userChannels.getOrDefault(userId, new ConcurrentHashMap<>());
    }

    /**
     * Broadcast message to all channel members
     */
    public void broadcastToChannel(Long channelId, Object message) {
        ChatChannel channel = channels.get(channelId);
        if (channel == null || !channel.isActive()) {
            return;
        }
        
        // This would be implemented with the WebSocket session manager
        // For now, it's a placeholder
        logger.debug("Broadcasting message to channel {} with {} members", 
                channelId, channel.getMemberCount());
    }

    /**
     * Add user to channel mapping
     */
    private void addUserToChannel(Long userId, Long channelId) {
        userChannels.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(channelId, true);
    }

    /**
     * Remove user from channel mapping
     */
    private void removeUserFromChannel(Long userId, Long channelId) {
        ConcurrentHashMap<Long, Boolean> channels = userChannels.get(userId);
        if (channels != null) {
            channels.remove(channelId);
            if (channels.isEmpty()) {
                userChannels.remove(userId);
            }
        }
    }

    /**
     * Get total number of channels
     */
    public int getChannelCount() {
        return channels.size();
    }

    /**
     * Get active channel count
     */
    public long getActiveChannelCount() {
        return channels.values().stream().filter(ChatChannel::isActive).count();
    }
}