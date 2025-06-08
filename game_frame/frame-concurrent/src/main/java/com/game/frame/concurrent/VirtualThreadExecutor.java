package com.game.frame.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Virtual Thread Executor Manager (Java 17 compatible)
 * Manages thread lifecycle with statistics and graceful shutdown
 * Note: Uses cached thread pool instead of virtual threads for Java 17 compatibility
 *
 * @author lx
 * @date 2024-01-01
 */
public class VirtualThreadExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadExecutor.class);
    
    private final ExecutorService executor;
    private final String namePrefix;
    private final AtomicLong submittedTasks = new AtomicLong(0);
    private final AtomicLong completedTasks = new AtomicLong(0);
    private final AtomicLong rejectedTasks = new AtomicLong(0);
    private final AtomicLong threadCounter = new AtomicLong(0);
    
    private volatile boolean shutdown = false;
    
    /**
     * Creates a new VirtualThreadExecutor with default name prefix
     */
    public VirtualThreadExecutor() {
        this("GameThread");
    }
    
    /**
     * Creates a new VirtualThreadExecutor with custom name prefix
     * 
     * @param namePrefix the prefix for thread names
     */
    public VirtualThreadExecutor(String namePrefix) {
        this.namePrefix = namePrefix;
        this.executor = Executors.newCachedThreadPool(this::createThread);
        logger.info("VirtualThreadExecutor created with namePrefix: {} (Java 17 mode)", namePrefix);
    }
    
    /**
     * Submits a task for execution
     * 
     * @param task the task to execute
     * @throws IllegalStateException if executor is shutdown
     */
    public void submit(Runnable task) {
        if (shutdown) {
            rejectedTasks.incrementAndGet();
            throw new IllegalStateException("Executor is shutdown");
        }
        
        submittedTasks.incrementAndGet();
        executor.submit(() -> {
            try {
                task.run();
                completedTasks.incrementAndGet();
            } catch (Exception e) {
                logger.error("Task execution failed", e);
            }
        });
    }
    
    private Thread createThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(namePrefix + "-" + threadCounter.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    }
    
    /**
     * Gets the number of submitted tasks
     * 
     * @return submitted task count
     */
    public long getSubmittedTaskCount() {
        return submittedTasks.get();
    }
    
    /**
     * Gets the number of completed tasks
     * 
     * @return completed task count
     */
    public long getCompletedTaskCount() {
        return completedTasks.get();
    }
    
    /**
     * Gets the number of rejected tasks
     * 
     * @return rejected task count
     */
    public long getRejectedTaskCount() {
        return rejectedTasks.get();
    }
    
    /**
     * Initiates graceful shutdown of the executor
     */
    public void shutdown() {
        if (!shutdown) {
            shutdown = true;
            executor.shutdown();
            logger.info("VirtualThreadExecutor shutdown initiated. Stats - Submitted: {}, Completed: {}, Rejected: {}", 
                    submittedTasks.get(), completedTasks.get(), rejectedTasks.get());
        }
    }
    
    /**
     * Forces immediate shutdown of the executor
     */
    public void shutdownNow() {
        if (!shutdown) {
            shutdown = true;
            executor.shutdownNow();
            logger.warn("VirtualThreadExecutor forced shutdown. Stats - Submitted: {}, Completed: {}, Rejected: {}", 
                    submittedTasks.get(), completedTasks.get(), rejectedTasks.get());
        }
    }
    
    /**
     * Checks if the executor is shutdown
     * 
     * @return true if shutdown
     */
    public boolean isShutdown() {
        return shutdown;
    }
}