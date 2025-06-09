package com.game.service.chat.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Chat channel model
 * Represents a chat channel with members and properties
 *
 * @author lx
 * @date 2025/01/08
 */
public class ChatChannel {
    
    private Long channelId;
    private ChannelType type;
    private String name;
    private Set<Long> members;
    private Map<String, Object> properties;
    private Long ownerId;
    private boolean active = true;

    /**
     * Channel types enumeration
     */
    public enum ChannelType {
        WORLD,      // World/Global channel
        GUILD,      // Guild channel
        PRIVATE,    // Private chat
        TEAM,       // Team/Group channel
        SYSTEM      // System channel
    }

    public ChatChannel() {
        this.members = new CopyOnWriteArraySet<>();
        this.properties = new ConcurrentHashMap<>();
    }

    public ChatChannel(Long channelId, ChannelType type, String name, Long ownerId) {
        this();
        this.channelId = channelId;
        this.type = type;
        this.name = name;
        this.ownerId = ownerId;
    }

    /**
     * Add member to channel
     */
    public boolean addMember(Long userId) {
        return members.add(userId);
    }

    /**
     * Remove member from channel
     */
    public boolean removeMember(Long userId) {
        return members.remove(userId);
    }

    /**
     * Check if user is member
     */
    public boolean isMember(Long userId) {
        return members.contains(userId);
    }

    /**
     * Get member count
     */
    public int getMemberCount() {
        return members.size();
    }

    // Getters and Setters
    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public ChannelType getType() {
        return type;
    }

    public void setType(ChannelType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Long> getMembers() {
        return members;
    }

    public void setMembers(Set<Long> members) {
        this.members = members != null ? members : new CopyOnWriteArraySet<>();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties != null ? properties : new ConcurrentHashMap<>();
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "ChatChannel{" +
                "channelId=" + channelId +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", memberCount=" + getMemberCount() +
                ", ownerId=" + ownerId +
                ", active=" + active +
                '}';
    }
}