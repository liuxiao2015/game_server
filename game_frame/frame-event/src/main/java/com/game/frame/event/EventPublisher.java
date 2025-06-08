package com.game.frame.event;

import java.util.List;

/**
 * Event publisher interface for publishing events
 * Provides synchronous/asynchronous publishing and batch publishing
 *
 * @author lx
 * @date 2024-01-01
 */
public interface EventPublisher {
    
    /**
     * Publishes an event synchronously
     * All listeners will be executed before this method returns
     * 
     * @param event the event to publish
     */
    void publish(Event event);
    
    /**
     * Publishes an event asynchronously
     * Returns immediately, event processing happens in background
     * 
     * @param event the event to publish
     */
    void publishAsync(Event event);
    
    /**
     * Publishes multiple events in batch
     * More efficient than publishing events one by one
     * 
     * @param events list of events to publish
     */
    void publishBatch(List<Event> events);
    
    /**
     * Publishes multiple events in batch asynchronously
     * 
     * @param events list of events to publish
     */
    void publishBatchAsync(List<Event> events);
}