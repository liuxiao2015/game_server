package com.game.frame.event.distributed;

import com.game.frame.event.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event serializer for distributed event processing
 * Supports JSON and Protobuf serialization
 *
 * @author lx
 * @date 2024-01-01
 */
public class EventSerializer {

    private static final Logger logger = LoggerFactory.getLogger(EventSerializer.class);

    private final ObjectMapper objectMapper;

    public EventSerializer() {
        this.objectMapper = new ObjectMapper();
        // Configure object mapper for event serialization
        this.objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    /**
     * Serialize event to JSON string
     *
     * @param event the event to serialize
     * @return JSON string
     */
    public String serializeToJson(Event event) {
        try {
            EventWrapper wrapper = new EventWrapper(event);
            return objectMapper.writeValueAsString(wrapper);
        } catch (Exception e) {
            logger.error("Failed to serialize event to JSON: {}", event.getClass().getSimpleName(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }

    /**
     * Deserialize event from JSON string
     *
     * @param json JSON string
     * @return event object
     */
    public Event deserializeFromJson(String json) {
        try {
            EventWrapper wrapper = objectMapper.readValue(json, EventWrapper.class);
            return wrapper.getEvent();
        } catch (Exception e) {
            logger.error("Failed to deserialize event from JSON", e);
            throw new RuntimeException("Event deserialization failed", e);
        }
    }

    /**
     * Serialize event to byte array
     *
     * @param event the event to serialize
     * @return byte array
     */
    public byte[] serializeToBytes(Event event) {
        try {
            String json = serializeToJson(event);
            return json.getBytes("UTF-8");
        } catch (Exception e) {
            logger.error("Failed to serialize event to bytes: {}", event.getClass().getSimpleName(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }

    /**
     * Deserialize event from byte array
     *
     * @param bytes byte array
     * @return event object
     */
    public Event deserializeFromBytes(byte[] bytes) {
        try {
            String json = new String(bytes, "UTF-8");
            return deserializeFromJson(json);
        } catch (Exception e) {
            logger.error("Failed to deserialize event from bytes", e);
            throw new RuntimeException("Event deserialization failed", e);
        }
    }

    /**
     * Event wrapper for serialization
     */
    public static class EventWrapper {
        private String eventType;
        private Event event;
        private long serializeTime;

        public EventWrapper() {
            this.serializeTime = System.currentTimeMillis();
        }

        public EventWrapper(Event event) {
            this();
            this.event = event;
            this.eventType = event.getClass().getName();
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public Event getEvent() {
            return event;
        }

        public void setEvent(Event event) {
            this.event = event;
        }

        public long getSerializeTime() {
            return serializeTime;
        }

        public void setSerializeTime(long serializeTime) {
            this.serializeTime = serializeTime;
        }
    }
}