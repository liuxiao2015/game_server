package com.game.service.chat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Chat message entity for ElasticSearch storage
 * Represents a chat message with all metadata
 *
 * @author lx
 * @date 2025/01/08
 */
@Document(indexName = "chat_messages")
public class ChatMessage {
    
    @Id
    private String messageId;
    
    @Field(type = FieldType.Long)
    private Long senderId;
    
    @Field(type = FieldType.Long)
    private Long channelId;
    
    @Field(type = FieldType.Keyword)
    private MessageType type;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;
    
    @Field(type = FieldType.Date)
    private LocalDateTime timestamp;
    
    @Field(type = FieldType.Object)
    private Map<String, Object> extra; // Emotions, images, etc.
    
    @Field(type = FieldType.Boolean)
    private boolean deleted = false;

    /**
     * Message types enumeration
     */
    public enum MessageType {
        TEXT,        // Text message
        IMAGE,       // Image message
        VOICE,       // Voice message
        SYSTEM,      // System message
        EMOTION      // Emotion/sticker
    }

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String messageId, Long senderId, Long channelId, MessageType type, String content) {
        this();
        this.messageId = messageId;
        this.senderId = senderId;
        this.channelId = channelId;
        this.type = type;
        this.content = content;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId='" + messageId + '\'' +
                ", senderId=" + senderId +
                ", channelId=" + channelId +
                ", type=" + type +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", extra=" + extra +
                ", deleted=" + deleted +
                '}';
    }
}