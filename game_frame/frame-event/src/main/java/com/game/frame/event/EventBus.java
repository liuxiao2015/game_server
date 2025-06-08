package com.game.frame.event;

import com.game.frame.concurrent.VirtualThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Local event bus implementation based on virtual threads
 * Supports event priority, filtering, and asynchronous processing
 *
 * @author lx
 * @date 2024-01-01
 */
public class EventBus implements EventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    
    private final VirtualThreadExecutor executor;
    private final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<EventListenerInfo>> listeners;
    private final List<Predicate<Event>> eventFilters;
    
    /**
     * Creates a new EventBus with default executor
     */
    public EventBus() {
        this(new VirtualThreadExecutor("EventBus"));
    }
    
    /**
     * Creates a new EventBus with custom executor
     * 
     * @param executor the virtual thread executor to use
     */
    public EventBus(VirtualThreadExecutor executor) {
        this.executor = executor;
        this.listeners = new ConcurrentHashMap<>();
        this.eventFilters = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Registers an event listener object
     * Scans for methods annotated with @EventListener
     * 
     * @param listener the listener object
     */
    public void register(Object listener) {
        Class<?> listenerClass = listener.getClass();
        Method[] methods = listenerClass.getDeclaredMethods();
        
        for (Method method : methods) {
            EventListener annotation = method.getAnnotation(EventListener.class);
            if (annotation != null) {
                registerListenerMethod(listener, method, annotation);
            }
        }
        
        logger.info("Registered event listener: {}", listenerClass.getSimpleName());
    }
    
    /**
     * Unregisters an event listener object
     * 
     * @param listener the listener object to remove
     */
    public void unregister(Object listener) {
        listeners.values().forEach(listenerList -> 
            listenerList.removeIf(info -> info.getListener() == listener));
        logger.info("Unregistered event listener: {}", listener.getClass().getSimpleName());
    }
    
    /**
     * Adds an event filter
     * Events that don't pass the filter will not be delivered
     * 
     * @param filter the event filter predicate
     */
    public void addEventFilter(Predicate<Event> filter) {
        eventFilters.add(filter);
    }
    
    /**
     * Removes an event filter
     * 
     * @param filter the event filter to remove
     */
    public void removeEventFilter(Predicate<Event> filter) {
        eventFilters.remove(filter);
    }
    
    @Override
    public void publish(Event event) {
        if (!passesFilters(event)) {
            return;
        }
        
        List<EventListenerInfo> eventListeners = getListenersForEvent(event);
        
        for (EventListenerInfo listenerInfo : eventListeners) {
            try {
                if (listenerInfo.isAsync()) {
                    executor.submit(() -> invokeListener(listenerInfo, event));
                } else {
                    invokeListener(listenerInfo, event);
                }
            } catch (Exception e) {
                logger.error("Error publishing event to listener", e);
            }
        }
    }
    
    @Override
    public void publishAsync(Event event) {
        executor.submit(() -> publish(event));
    }
    
    @Override
    public void publishBatch(List<Event> events) {
        events.forEach(this::publish);
    }
    
    @Override
    public void publishBatchAsync(List<Event> events) {
        executor.submit(() -> publishBatch(events));
    }
    
    /**
     * Gracefully shuts down the event bus
     */
    public void shutdown() {
        executor.shutdown();
        listeners.clear();
        eventFilters.clear();
        logger.info("EventBus shutdown completed");
    }
    
    private void registerListenerMethod(Object listener, Method method, EventListener annotation) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1 || !Event.class.isAssignableFrom(paramTypes[0])) {
            logger.warn("Event listener method {} must have exactly one Event parameter", method.getName());
            return;
        }
        
        @SuppressWarnings("unchecked")
        Class<? extends Event> eventType = (Class<? extends Event>) paramTypes[0];
        
        EventListenerInfo listenerInfo = new EventListenerInfo(
                listener, method, annotation.priority(), annotation.async());
        
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(listenerInfo);
        
        // Sort by priority (higher priority first)
        listeners.get(eventType).sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
    }
    
    private List<EventListenerInfo> getListenersForEvent(Event event) {
        return listeners.entrySet().stream()
                .filter(entry -> entry.getKey().isAssignableFrom(event.getClass()))
                .flatMap(entry -> entry.getValue().stream())
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .toList();
    }
    
    private boolean passesFilters(Event event) {
        return eventFilters.stream().allMatch(filter -> filter.test(event));
    }
    
    private void invokeListener(EventListenerInfo listenerInfo, Event event) {
        try {
            listenerInfo.getMethod().invoke(listenerInfo.getListener(), event);
        } catch (Exception e) {
            logger.error("Error invoking event listener: {}", listenerInfo.getMethod().getName(), e);
        }
    }
    
    /**
     * Internal class to hold event listener information
     */
    private static class EventListenerInfo {
        private final Object listener;
        private final Method method;
        private final int priority;
        private final boolean async;
        
        public EventListenerInfo(Object listener, Method method, int priority, boolean async) {
            this.listener = listener;
            this.method = method;
            this.priority = priority;
            this.async = async;
            this.method.setAccessible(true);
        }
        
        public Object getListener() {
            return listener;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public int getPriority() {
            return priority;
        }
        
        public boolean isAsync() {
            return async;
        }
    }
}