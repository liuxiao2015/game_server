package com.game.frame.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Concurrent utilities for batch task execution, timeout control, and retry mechanism
 *
 * @author lx
 * @date 2024-01-01
 */
public class ConcurrentUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentUtils.class);
    
    /**
     * Maximum retry attempts
     */
    public static final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * Default retry delay in milliseconds
     */
    public static final long DEFAULT_RETRY_DELAY_MS = 1000;
    
    /**
     * Executes a batch of tasks with specified batch size
     * 
     * @param tasks list of tasks to execute
     * @param batchSize maximum batch size
     * @param timeout timeout for each batch
     * @param <T> result type
     * @return list of all task results
     */
    public static <T> List<TaskResult<T>> executeBatch(List<Callable<T>> tasks, int batchSize, Duration timeout) {
        if (tasks.isEmpty()) {
            return List.of();
        }
        
        logger.info("Executing {} tasks in batches of {}", tasks.size(), batchSize);
        
        List<TaskResult<T>> allResults = new java.util.ArrayList<>();
        
        for (int i = 0; i < tasks.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, tasks.size());
            List<Callable<T>> batch = tasks.subList(i, endIndex);
            
            logger.debug("Processing batch {}-{}", i, endIndex - 1);
            List<TaskResult<T>> batchResults = StructuredTaskManager.executeAll(batch, timeout);
            allResults.addAll(batchResults);
        }
        
        long successCount = allResults.stream()
                .mapToLong(result -> result.isSuccess() ? 1 : 0)
                .sum();
        
        logger.info("Batch execution completed. Success: {}, Failed: {}", 
                successCount, allResults.size() - successCount);
        
        return allResults;
    }
    
    /**
     * Executes a task with retry mechanism
     * 
     * @param task the task to execute
     * @param maxAttempts maximum retry attempts
     * @param retryDelayMs delay between retries in milliseconds
     * @param <T> result type
     * @return task result
     */
    public static <T> TaskResult<T> executeWithRetry(Callable<T> task, int maxAttempts, long retryDelayMs) {
        Exception lastException = null;
        long totalStartTime = System.currentTimeMillis();
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            long attemptStartTime = System.currentTimeMillis();
            
            try {
                T result = task.call();
                long executionTime = System.currentTimeMillis() - totalStartTime;
                logger.debug("Task succeeded on attempt {}/{}", attempt, maxAttempts);
                return TaskResult.success(result, executionTime);
            } catch (Exception e) {
                lastException = e;
                logger.warn("Task failed on attempt {}/{}: {}", attempt, maxAttempts, e.getMessage());
                
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        long totalExecutionTime = System.currentTimeMillis() - totalStartTime;
        logger.error("Task failed after {} attempts", maxAttempts, lastException);
        return TaskResult.failure(lastException, totalExecutionTime);
    }
    
    /**
     * Executes a task with default retry settings
     * 
     * @param task the task to execute
     * @param <T> result type
     * @return task result
     */
    public static <T> TaskResult<T> executeWithRetry(Callable<T> task) {
        return executeWithRetry(task, MAX_RETRY_ATTEMPTS, DEFAULT_RETRY_DELAY_MS);
    }
    
    /**
     * Executes a supplier with timeout using CompletableFuture
     * 
     * @param supplier the supplier to execute
     * @param timeout timeout duration
     * @param <T> result type
     * @return task result
     */
    public static <T> TaskResult<T> executeWithTimeout(Supplier<T> supplier, Duration timeout) {
        long startTime = System.currentTimeMillis();
        
        try {
            CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, executor);
            T result = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            long executionTime = System.currentTimeMillis() - startTime;
            return TaskResult.success(result, executionTime);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Task execution with timeout failed", e);
            return TaskResult.failure(e, executionTime);
        }
    }
    
    /**
     * Delays execution by specified duration
     * 
     * @param duration delay duration
     */
    public static void delay(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Delay interrupted", e);
        }
    }
}