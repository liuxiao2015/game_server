package com.game.service.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Chat service configuration properties
 * Manages configuration for WebSocket, RocketMQ, ElasticSearch and chat channels
 *
 * @author lx
 * @date 2025/01/08
 */
@Configuration
@ConfigurationProperties(prefix = "chat")
/**
 * ChatService配置类
 * 
 * 功能说明：
 * - 配置系统或模块的参数和属性
 * - 支持配置的自动加载和验证
 * - 集成Spring Boot配置管理机制
 *
 * @author lx
 * @date 2024-01-01
 */
public class ChatServiceConfig {

    /**
     * WebSocket configuration
     */
    private WebSocketProperties websocket = new WebSocketProperties();
    
    /**
     * RocketMQ configuration
     */
    private RocketMQProperties rocketmq = new RocketMQProperties();
    
    /**
     * ElasticSearch configuration
     */
    private ElasticSearchProperties elasticsearch = new ElasticSearchProperties();
    
    /**
     * Channel configuration
     */
    private ChannelProperties channel = new ChannelProperties();

    // Getters and Setters
    public WebSocketProperties getWebsocket() {
        return websocket;
    }

    public void setWebsocket(WebSocketProperties websocket) {
        this.websocket = websocket;
    }

    public RocketMQProperties getRocketmq() {
        return rocketmq;
    }

    public void setRocketmq(RocketMQProperties rocketmq) {
        this.rocketmq = rocketmq;
    }

    public ElasticSearchProperties getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(ElasticSearchProperties elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public ChannelProperties getChannel() {
        return channel;
    }

    public void setChannel(ChannelProperties channel) {
        this.channel = channel;
    }

    /**
     * WebSocket properties
     */
    public static class WebSocketProperties {
        private String endpoint = "/chat";
        private int heartbeatInterval = 30000; // 30 seconds
        private int maxSessionTimeout = 300000; // 5 minutes

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public int getHeartbeatInterval() {
            return heartbeatInterval;
        }

        public void setHeartbeatInterval(int heartbeatInterval) {
            this.heartbeatInterval = heartbeatInterval;
        }

        public int getMaxSessionTimeout() {
            return maxSessionTimeout;
        }

        public void setMaxSessionTimeout(int maxSessionTimeout) {
            this.maxSessionTimeout = maxSessionTimeout;
        }
    }

    /**
     * RocketMQ properties
     */
    public static class RocketMQProperties {
        private String chatTopic = "CHAT_MESSAGES";
        private String offlineTopic = "OFFLINE_MESSAGES";
        private String broadcastTopic = "BROADCAST_MESSAGES";

        public String getChatTopic() {
            return chatTopic;
        }

        public void setChatTopic(String chatTopic) {
            this.chatTopic = chatTopic;
        }

        public String getOfflineTopic() {
            return offlineTopic;
        }

        public void setOfflineTopic(String offlineTopic) {
            this.offlineTopic = offlineTopic;
        }

        public String getBroadcastTopic() {
            return broadcastTopic;
        }

        public void setBroadcastTopic(String broadcastTopic) {
            this.broadcastTopic = broadcastTopic;
        }
    }

    /**
     * ElasticSearch properties
     */
    public static class ElasticSearchProperties {
        private String messageIndex = "chat_messages";
        private int maxSearchResults = 100;
        private int retentionDays = 90;

        public String getMessageIndex() {
            return messageIndex;
        }

        public void setMessageIndex(String messageIndex) {
            this.messageIndex = messageIndex;
        }

        public int getMaxSearchResults() {
            return maxSearchResults;
        }

        public void setMaxSearchResults(int maxSearchResults) {
            this.maxSearchResults = maxSearchResults;
        }

        public int getRetentionDays() {
            return retentionDays;
        }

        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }
    }

    /**
     * Channel properties
     */
    public static class ChannelProperties {
        private int maxMembersPerChannel = 1000;
        private int maxChannelsPerUser = 50;
        private boolean enableSensitiveWordFilter = true;

        public int getMaxMembersPerChannel() {
            return maxMembersPerChannel;
        }

        public void setMaxMembersPerChannel(int maxMembersPerChannel) {
            this.maxMembersPerChannel = maxMembersPerChannel;
        }

        public int getMaxChannelsPerUser() {
            return maxChannelsPerUser;
        }

        public void setMaxChannelsPerUser(int maxChannelsPerUser) {
            this.maxChannelsPerUser = maxChannelsPerUser;
        }

        public boolean isEnableSensitiveWordFilter() {
            return enableSensitiveWordFilter;
        }

        public void setEnableSensitiveWordFilter(boolean enableSensitiveWordFilter) {
            this.enableSensitiveWordFilter = enableSensitiveWordFilter;
        }
    }
}