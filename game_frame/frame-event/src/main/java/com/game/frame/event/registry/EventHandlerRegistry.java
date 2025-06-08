package com.game.frame.event.registry;

import com.game.frame.event.Event;
import com.game.frame.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event handler registry with annotation scanning, priority ordering, and exception isolation
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
public class EventHandlerRegistry {

    private static final Logger logger = LoggerFactory.getLogger(EventHandlerRegistry.class);

    private final Map<Class<? extends Event>, List<EventHandlerInfo>> eventHandlers = new ConcurrentHashMap<>();
    private final Map<Object, List<EventHandlerInfo>> listenerHandlers = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        logger.info("EventHandlerRegistry initialized");
    }

    /**
     * Register an event listener object
     */
    public void registerListener(Object listener) {
        if (listener == null) {
            return;
        }

        List<EventHandlerInfo> handlers = new ArrayList<>();
        Method[] methods = listener.getClass().getDeclaredMethods();

        for (Method method : methods) {
            EventListener annotation = method.getAnnotation(EventListener.class);
            if (annotation != null) {
                EventHandlerInfo handlerInfo = createHandlerInfo(listener, method, annotation);
                if (handlerInfo != null) {
                    handlers.add(handlerInfo);
                    registerHandler(handlerInfo);
                }
            }
        }

        if (!handlers.isEmpty()) {
            listenerHandlers.put(listener, handlers);
            logger.info("Registered {} event handlers from listener: {}", 
                    handlers.size(), listener.getClass().getSimpleName());
        }
    }

    /**
     * Unregister an event listener object
     */
    public void unregisterListener(Object listener) {
        if (listener == null) {
            return;
        }

        List<EventHandlerInfo> handlers = listenerHandlers.remove(listener);
        if (handlers != null) {
            for (EventHandlerInfo handler : handlers) {
                unregisterHandler(handler);
            }
            logger.info("Unregistered {} event handlers from listener: {}", 
                    handlers.size(), listener.getClass().getSimpleName());
        }
    }

    /**
     * Get handlers for a specific event type
     */
    public List<EventHandlerInfo> getHandlers(Class<? extends Event> eventType) {
        List<EventHandlerInfo> handlers = eventHandlers.get(eventType);
        if (handlers == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(handlers);
    }

    /**
     * Get all handlers for an event (including parent class handlers)
     */
    public List<EventHandlerInfo> getAllHandlers(Event event) {
        List<EventHandlerInfo> allHandlers = new ArrayList<>();
        
        Class<?> eventClass = event.getClass();
        while (eventClass != null && Event.class.isAssignableFrom(eventClass)) {
            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) eventClass;
            List<EventHandlerInfo> handlers = eventHandlers.get(eventType);
            if (handlers != null) {
                allHandlers.addAll(handlers);
            }
            eventClass = eventClass.getSuperclass();
        }

        // Sort by priority (higher priority first)
        allHandlers.sort((h1, h2) -> Integer.compare(h2.getPriority(), h1.getPriority()));
        return allHandlers;
    }

    /**
     * Create handler info from method and annotation
     */
    private EventHandlerInfo createHandlerInfo(Object listener, Method method, EventListener annotation) {
        try {
            // Validate method signature
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 1 || !Event.class.isAssignableFrom(paramTypes[0])) {
                logger.warn("Invalid event handler method signature: {}.{}", 
                        listener.getClass().getSimpleName(), method.getName());
                return null;
            }

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventType = (Class<? extends Event>) paramTypes[0];
            
            method.setAccessible(true);
            
            return new EventHandlerInfo(
                    listener,
                    method,
                    eventType,
                    annotation.priority(),
                    annotation.async(),
                    ""  // No condition field in current EventListener
            );
            
        } catch (Exception e) {
            logger.error("Failed to create handler info for method: {}.{}", 
                    listener.getClass().getSimpleName(), method.getName(), e);
            return null;
        }
    }

    /**
     * Register a single handler
     */
    private void registerHandler(EventHandlerInfo handler) {
        eventHandlers.computeIfAbsent(handler.getEventType(), k -> new CopyOnWriteArrayList<>())
                .add(handler);
        
        logger.debug("Registered event handler: {} for event type: {}", 
                handler.getMethod().getName(), handler.getEventType().getSimpleName());
    }

    /**
     * Unregister a single handler
     */
    private void unregisterHandler(EventHandlerInfo handler) {
        List<EventHandlerInfo> handlers = eventHandlers.get(handler.getEventType());
        if (handlers != null) {
            handlers.remove(handler);
            if (handlers.isEmpty()) {
                eventHandlers.remove(handler.getEventType());
            }
        }
    }

    /**
     * Get registry statistics
     */
    public String getStatistics() {
        int totalHandlers = eventHandlers.values().stream()
                .mapToInt(List::size)
                .sum();
        
        return String.format("EventHandlerRegistry - Event types: %d, Total handlers: %d, Listeners: %d",
                eventHandlers.size(), totalHandlers, listenerHandlers.size());
    }

    /**
     * Clear all handlers
     */
    public void clear() {
        int handlerCount = eventHandlers.values().stream().mapToInt(List::size).sum();
        eventHandlers.clear();
        listenerHandlers.clear();
        logger.info("Cleared all event handlers: {}", handlerCount);
    }

    /**
     * Event handler information
     */
    public static class EventHandlerInfo {
        private final Object listener;
        private final Method method;
        private final Class<? extends Event> eventType;
        private final int priority;
        private final boolean async;
        private final String condition;

        public EventHandlerInfo(Object listener, Method method, Class<? extends Event> eventType, 
                               int priority, boolean async, String condition) {
            this.listener = listener;
            this.method = method;
            this.eventType = eventType;
            this.priority = priority;
            this.async = async;
            this.condition = condition;
        }

        public Object getListener() { return listener; }
        public Method getMethod() { return method; }
        public Class<? extends Event> getEventType() { return eventType; }
        public int getPriority() { return priority; }
        public boolean isAsync() { return async; }
        public String getCondition() { return condition; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EventHandlerInfo)) return false;
            EventHandlerInfo that = (EventHandlerInfo) o;
            return Objects.equals(listener, that.listener) && Objects.equals(method, that.method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listener, method);
        }

        @Override
        public String toString() {
            return String.format("EventHandler[%s.%s, priority=%d, async=%s]",
                    listener.getClass().getSimpleName(), method.getName(), priority, async);
        }
    }
}