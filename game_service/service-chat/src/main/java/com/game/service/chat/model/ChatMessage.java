package com.game.service.chat.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聊天消息实体类
 * 
 * 功能说明：
 * - 存储和管理游戏内聊天消息的完整信息
 * - 支持多种消息类型，包括文本、图片、语音等
 * - 集成ElasticSearch实现高性能的消息检索和分析
 * - 提供消息的逻辑删除和扩展属性管理
 * 
 * 设计思路：
 * - 使用ElasticSearch作为存储引擎，支持大量消息的快速检索
 * - 采用文档型数据结构，便于存储灵活的消息内容
 * - 支持消息的元数据管理，包括时间戳、发送者、频道等
 * - 提供扩展字段存储，支持表情、图片等富媒体内容
 * 
 * 存储策略：
 * - 使用ElasticSearch的文档存储特性
 * - 支持全文检索和复杂查询条件
 * - 提供字段级别的索引优化
 * - 支持消息的批量操作和聚合分析
 * 
 * 消息类型：
 * - TEXT：普通文本消息，支持全文检索
 * - IMAGE：图片消息，存储图片URL和缩略图
 * - VOICE：语音消息，存储语音文件URL和时长
 * - SYSTEM：系统消息，用于游戏内通知和公告
 * - EMOTION：表情和贴纸消息，增强交流体验
 * 
 * 应用场景：
 * - 游戏内实时聊天功能
 * - 频道/群组消息管理
 * - 消息历史记录查询
 * - 内容审核和违规检测
 * - 聊天数据统计和分析
 * 
 * 安全特性：
 * - 支持消息的逻辑删除，保护用户隐私
 * - 记录消息时间戳，便于审计和排序
 * - 支持消息内容的过滤和审核
 * - 提供发送者身份验证和权限控制
 *
 * @author lx
 * @date 2025/01/08
 */
@Document(indexName = "chat_messages")
public class ChatMessage {
    
    /** 消息唯一标识符，ElasticSearch文档ID */
    @Id
    private String messageId;
    
    /** 发送者用户ID，关联用户信息，支持消息归属和权限检查 */
    @Field(type = FieldType.Long)
    private Long senderId;
    
    /** 频道ID，标识消息所属的聊天频道或群组 */
    @Field(type = FieldType.Long)
    private Long channelId;
    
    /** 消息类型，枚举值，区分不同类型的消息内容 */
    @Field(type = FieldType.Keyword)
    private MessageType type;
    
    /** 消息内容，使用标准分析器支持全文检索 */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;
    
    /** 消息发送时间戳，用于消息排序和时间范围查询 */
    @Field(type = FieldType.Date)
    private LocalDateTime timestamp;
    
    /** 扩展属性，存储表情、图片URL、语音时长等额外信息 */
    @Field(type = FieldType.Object)
    private Map<String, Object> extra;
    
    /** 删除标记，支持消息的逻辑删除而不是物理删除 */
    @Field(type = FieldType.Boolean)
    private boolean deleted = false;

    /**
     * 消息类型枚举
     * 
     * 功能说明：
     * - 定义聊天系统支持的所有消息类型
     * - 便于消息的分类处理和展示渲染
     * - 支持不同类型消息的特殊处理逻辑
     * 
     * 类型说明：
     * - 基础类型：文本、图片、语音等常规消息
     * - 系统类型：系统通知、游戏事件等自动消息
     * - 交互类型：表情、贴纸等增强交流体验的消息
     */
    public enum MessageType {
        /** 文本消息 - 普通的文字聊天内容 */
        TEXT,
        
        /** 图片消息 - 包含图片的消息，存储图片URL */
        IMAGE,
        
        /** 语音消息 - 语音录音，存储音频文件URL和时长 */
        VOICE,
        
        /** 系统消息 - 游戏系统发送的通知和公告 */
        SYSTEM,
        
        /** 表情消息 - 表情包和贴纸，增强聊天体验 */
        EMOTION
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