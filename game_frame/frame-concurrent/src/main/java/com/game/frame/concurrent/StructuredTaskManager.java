package com.game.frame.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Structured Task Manager (Java 17 compatible)
 * Task orchestration using CompletableFuture instead of StructuredTaskScope
 *
 * @author lx
 * @date 2024-01-01
 */
public class StructuredTaskManager {
    
    private static final Logger logger = LoggerFactory.getLogger(StructuredTaskManager.class);
    
    private static final ExecutorService defaultExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "StructuredTask-" + System.nanoTime());
        t.setDaemon(true);
        return t;
    });
    
    /**
     * Executes multiple tasks concurrently and waits for all to complete
     * 
     * @param tasks list of tasks to execute
     * @param timeout maximum wait time
     * @param <T> result type
     * @return list of task results
     */
    public static <T> List<TaskResult<T>> executeAll(List<Callable<T>> tasks, Duration timeout) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<CompletableFuture<T>> futures = tasks.stream()
                    .map(task -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return task.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, defaultExecutor))
                    .toList();
            
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            
            // Wait for completion with timeout
            allFutures.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            
            // Collect results
            return futures.stream()
                    .map(future -> {
                        try {
                            long executionTime = System.currentTimeMillis() - startTime;
                            return TaskResult.success(future.get(), executionTime);
                        } catch (Exception e) {
                            long executionTime = System.currentTimeMillis() - startTime;
                            return TaskResult.<T>failure(e, executionTime);
                        }
                    })
                    .toList();
                    
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Task execution failed", e);
            return List.of(TaskResult.failure(e, executionTime));
        }
    }
    
    /**
     * Executes multiple tasks concurrently and returns the first successful result
     * 
     * @param tasks list of tasks to execute
     * @param timeout maximum wait time
     * @param <T> result type
     * @return first successful task result
     */
    public static <T> TaskResult<T> executeAny(List<Callable<T>> tasks, Duration timeout) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<CompletableFuture<T>> futures = tasks.stream()
                    .map(task -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return task.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, defaultExecutor))
                    .toList();
            
            CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(
                    futures.toArray(new CompletableFuture[0]));
            
            @SuppressWarnings("unchecked")
            T result = (T) anyFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            
            long executionTime = System.currentTimeMillis() - startTime;
            return TaskResult.success(result, executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Task execution failed", e);
            return TaskResult.failure(e, executionTime);
        }
    }
    
    /**
     * Executes a single task with timeout control
     * 
     * @param task the task to execute
     * @param timeout maximum execution time
     * @param <T> result type
     * @return task result
     */
    public static <T> TaskResult<T> executeWithTimeout(Callable<T> task, Duration timeout) {
        return executeAll(List.of(task), timeout).get(0);
    }
    
    /**
     * Executes a supplier task with exception handling
     * 
     * @param supplier the supplier to execute
     * @param <T> result type
     * @return task result
     */
    public static <T> TaskResult<T> safeExecute(Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        
        try {
            T result = supplier.get();
            long executionTime = System.currentTimeMillis() - startTime;
            return TaskResult.success(result, executionTime);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("Safe execution failed", e);
            return TaskResult.failure(e, executionTime);
        }
    }
}