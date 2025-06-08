package com.game.frame.event;

import java.time.Instant;

/**
 * Base event class containing common event properties
 *
 * @author lx
 * @date 2024-01-01
 */
public abstract class Event {
    
    private final String eventType;
    private final Instant timestamp;
    private final String source;
    
    /**
     * Creates a new event
     * 
     * @param eventType the type of event
     * @param source the source of the event
     */
    protected Event(String eventType, String source) {
        this.eventType = eventType;
        this.source = source;
        this.timestamp = Instant.now();
    }
    
    /**
     * Gets the event type
     * 
     * @return event type
     */
    public String getEventType() {
        return eventType;
    }
    
    /**
     * Gets the event timestamp
     * 
     * @return timestamp when event was created
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the event source
     * 
     * @return event source identifier
     */
    public String getSource() {
        return source;
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                '}';
    }
}