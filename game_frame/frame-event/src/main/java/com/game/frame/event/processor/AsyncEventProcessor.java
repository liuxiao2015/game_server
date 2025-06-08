package com.game.frame.event.processor;

import com.game.frame.event.Event;
import com.game.frame.event.registry.EventHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Asynchronous event processor using virtual thread pool and batch processing
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
public class AsyncEventProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncEventProcessor.class);

    private final EventHandlerRegistry handlerRegistry;
    private final ExecutorService virtualThreadExecutor;
    private final BlockingQueue<EventTask> eventQueue;
    private final ScheduledExecutorService scheduledExecutor;

    private final AtomicLong processedEvents = new AtomicLong(0);
    private final AtomicLong failedEvents = new AtomicLong(0);
    private volatile boolean running = false;

    // Batch processing configuration
    private static final int BATCH_SIZE = 100;
    private static final int BATCH_TIMEOUT_MS = 10;
    private static final int QUEUE_CAPACITY = 10000;

    public AsyncEventProcessor(EventHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
        this.virtualThreadExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "AsyncEventProcessor-Worker");
            t.setDaemon(true);
            return t;
        });
        this.eventQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        this.scheduledExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "AsyncEventProcessor-Scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    @PostConstruct
    public void start() {
        running = true;
        
        // Start batch processor
        scheduledExecutor.scheduleAtFixedRate(this::processBatch, 0, BATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        
        logger.info("AsyncEventProcessor started with batch size: {}, timeout: {}ms", 
                BATCH_SIZE, BATCH_TIMEOUT_MS);
    }

    @PreDestroy
    public void stop() {
        running = false;
        
        // Process remaining events
        processBatch();
        
        // Shutdown executors
        scheduledExecutor.shutdown();
        virtualThreadExecutor.shutdown();
        
        try {
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
            if (!virtualThreadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                virtualThreadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduledExecutor.shutdownNow();
            virtualThreadExecutor.shutdownNow();
        }
        
        logger.info("AsyncEventProcessor stopped. Stats - Processed: {}, Failed: {}", 
                processedEvents.get(), failedEvents.get());
    }

    /**
     * Submit event for asynchronous processing
     */
    public boolean submitEvent(Event event, EventHandlerRegistry.EventHandlerInfo handlerInfo) {
        if (!running) {
            logger.warn("AsyncEventProcessor is not running, dropping event: {}", 
                    event.getClass().getSimpleName());
            return false;
        }

        EventTask task = new EventTask(event, handlerInfo);
        boolean submitted = eventQueue.offer(task);
        
        if (!submitted) {
            logger.warn("Event queue is full, dropping event: {}", event.getClass().getSimpleName());
            failedEvents.incrementAndGet();
        }
        
        return submitted;
    }

    /**
     * Process events in batches
     */
    private void processBatch() {
        if (eventQueue.isEmpty()) {
            return;
        }

        List<EventTask> batch = new ArrayList<>(BATCH_SIZE);
        eventQueue.drainTo(batch, BATCH_SIZE);
        
        if (batch.isEmpty()) {
            return;
        }

        // Group events by handler for better efficiency
        batch.parallelStream().forEach(task -> {
            virtualThreadExecutor.submit(() -> processEventTask(task));
        });
        
        logger.debug("Submitted batch of {} events for processing", batch.size());
    }

    /**
     * Process a single event task
     */
    private void processEventTask(EventTask task) {
        try {
            long startTime = System.nanoTime();
            
            // Invoke handler method
            task.handlerInfo.getMethod().invoke(task.handlerInfo.getListener(), task.event);
            
            long duration = System.nanoTime() - startTime;
            processedEvents.incrementAndGet();
            
            logger.debug("Processed event {} in {}ns", 
                    task.event.getClass().getSimpleName(), duration);
            
        } catch (Exception e) {
            failedEvents.incrementAndGet();
            logger.error("Failed to process event {} with handler {}: {}", 
                    task.event.getClass().getSimpleName(),
                    task.handlerInfo.getMethod().getName(),
                    e.getMessage(), e);
        }
    }

    /**
     * Get processing statistics
     */
    public ProcessorStats getStats() {
        return new ProcessorStats(
                processedEvents.get(),
                failedEvents.get(),
                eventQueue.size(),
                running
        );
    }

    /**
     * Event task wrapper
     */
    private static class EventTask {
        final Event event;
        final EventHandlerRegistry.EventHandlerInfo handlerInfo;
        final long submitTime;

        EventTask(Event event, EventHandlerRegistry.EventHandlerInfo handlerInfo) {
            this.event = event;
            this.handlerInfo = handlerInfo;
            this.submitTime = System.currentTimeMillis();
        }
    }

    /**
     * Processor statistics
     */
    public static class ProcessorStats {
        private final long processedEvents;
        private final long failedEvents;
        private final int queueSize;
        private final boolean running;

        public ProcessorStats(long processedEvents, long failedEvents, int queueSize, boolean running) {
            this.processedEvents = processedEvents;
            this.failedEvents = failedEvents;
            this.queueSize = queueSize;
            this.running = running;
        }

        public long getProcessedEvents() { return processedEvents; }
        public long getFailedEvents() { return failedEvents; }
        public int getQueueSize() { return queueSize; }
        public boolean isRunning() { return running; }
        public double getSuccessRate() {
            long total = processedEvents + failedEvents;
            return total > 0 ? (double) processedEvents / total * 100 : 0;
        }

        @Override
        public String toString() {
            return String.format("ProcessorStats[processed=%d, failed=%d, queued=%d, running=%s, success=%.2f%%]",
                    processedEvents, failedEvents, queueSize, running, getSuccessRate());
        }
    }
}