package com.game.frame.event.distributed;

import com.game.frame.event.Event;
import com.game.frame.event.EventBus;
import com.game.frame.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Distributed event bus implementation based on Redis pub/sub
 * Provides cross-service event communication capabilities
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
/**
 * DistributedEventBus
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
public class DistributedEventBus implements EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(DistributedEventBus.class);

    private static final String EVENT_CHANNEL_PREFIX = "game:events:";
    private static final String GLOBAL_EVENT_CHANNEL = EVENT_CHANNEL_PREFIX + "global";

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final EventBus localEventBus;
    private final EventSerializer eventSerializer;

    private final ConcurrentHashMap<String, ChannelTopic> subscribedChannels = new ConcurrentHashMap<>();
    private final AtomicLong publishedEvents = new AtomicLong(0);
    private final AtomicLong receivedEvents = new AtomicLong(0);

    public DistributedEventBus(RedisTemplate<String, Object> redisTemplate,
                               RedisMessageListenerContainer listenerContainer,
                               EventBus localEventBus) {
        this.redisTemplate = redisTemplate;
        this.listenerContainer = listenerContainer;
        this.localEventBus = localEventBus;
        this.eventSerializer = new EventSerializer();
    }

    @PostConstruct
    public void init() {
        // Subscribe to global event channel
        subscribeToChannel(GLOBAL_EVENT_CHANNEL);
        
        // Subscribe to service-specific channel
        String serviceChannel = EVENT_CHANNEL_PREFIX + serviceName;
        subscribeToChannel(serviceChannel);
        
        logger.info("DistributedEventBus initialized for service: {}", serviceName);
    }

    @PreDestroy
    public void destroy() {
        if (listenerContainer != null) {
            listenerContainer.stop();
        }
        logger.info("DistributedEventBus destroyed. Stats - Published: {}, Received: {}", 
                publishedEvents.get(), receivedEvents.get());
    }

    @Override
    public void publish(Event event) {
        try {
            if (event instanceof RemoteEvent) {
                publishRemoteEvent((RemoteEvent) event);
            } else {
                // Publish to local event bus only
                localEventBus.publish(event);
            }
        } catch (Exception e) {
            logger.error("Failed to publish event: {}", event.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void publishAsync(Event event) {
        // Use virtual thread for async publishing (Java 17 compatible)
        CompletableFuture.runAsync(() -> publish(event));
    }

    @Override
    public void publishBatch(List<Event> events) {
        events.forEach(this::publish);
    }

    @Override
    public void publishBatchAsync(List<Event> events) {
        CompletableFuture.runAsync(() -> publishBatch(events));
    }

    /**
     * Publish remote event to distributed channels
     */
    private void publishRemoteEvent(RemoteEvent event) {
        try {
            // Set source service if not set
            if (event.getSourceService() == null) {
                event.setSourceService(serviceName);
            }

            // Determine target channel
            String channel = determineTargetChannel(event);
            
            // Serialize event
            String serializedEvent = eventSerializer.serializeToJson(event);
            
            // Publish to Redis
            redisTemplate.convertAndSend(channel, serializedEvent);
            publishedEvents.incrementAndGet();
            
            logger.debug("Published remote event {} to channel {}", 
                    event.getClass().getSimpleName(), channel);
            
        } catch (Exception e) {
            logger.error("Failed to publish remote event: {}", event.getClass().getSimpleName(), e);
        }
    }

    /**
     * Determine target channel for remote event
     */
    private String determineTargetChannel(RemoteEvent event) {
        if (event.getTargetService() != null && !"*".equals(event.getTargetService())) {
            return EVENT_CHANNEL_PREFIX + event.getTargetService();
        } else {
            return GLOBAL_EVENT_CHANNEL;
        }
    }

    /**
     * Subscribe to a Redis channel
     */
    private void subscribeToChannel(String channel) {
        try {
            ChannelTopic topic = new ChannelTopic(channel);
            MessageListenerAdapter listener = new MessageListenerAdapter(this, "handleRedisMessage");
            
            listenerContainer.addMessageListener(listener, topic);
            subscribedChannels.put(channel, topic);
            
            logger.info("Subscribed to channel: {}", channel);
        } catch (Exception e) {
            logger.error("Failed to subscribe to channel: {}", channel, e);
        }
    }

    /**
     * Handle incoming Redis messages
     */
    public void handleRedisMessage(String message, String channel) {
        try {
            receivedEvents.incrementAndGet();
            
            // Deserialize event
            Event event = eventSerializer.deserializeFromJson(message);
            
            // Check if it's a remote event and if we should process it
            if (event instanceof RemoteEvent) {
                RemoteEvent remoteEvent = (RemoteEvent) event;
                
                // Don't process events from this service
                if (serviceName.equals(remoteEvent.getSourceService())) {
                    return;
                }
                
                // Check if event is targeted to this service
                if (!remoteEvent.isTargetedTo(serviceName)) {
                    return;
                }
                
                logger.debug("Received remote event {} from service {} on channel {}", 
                        event.getClass().getSimpleName(), remoteEvent.getSourceService(), channel);
            }
            
            // Publish to local event bus
            localEventBus.publish(event);
            
        } catch (Exception e) {
            logger.error("Failed to handle Redis message from channel {}", channel, e);
        }
    }

    /**
     * Get statistics
     */
    public String getStatistics() {
        return String.format("DistributedEventBus[%s] - Published: %d, Received: %d, Channels: %d", 
                serviceName, publishedEvents.get(), receivedEvents.get(), subscribedChannels.size());
    }

    /**
     * Subscribe to additional channel
     */
    public void subscribeToCustomChannel(String channelName) {
        String fullChannel = EVENT_CHANNEL_PREFIX + channelName;
        if (!subscribedChannels.containsKey(fullChannel)) {
            subscribeToChannel(fullChannel);
        }
    }
}