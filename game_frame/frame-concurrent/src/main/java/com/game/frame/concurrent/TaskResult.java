package com.game.frame.concurrent;

import java.time.Instant;

/**
 * Task execution result wrapper
 * Contains success/failure status, result data, exception info, and execution time
 *
 * @author lx
 * @date 2024-01-01
 */
public class TaskResult<T> {
    
    private final boolean success;
    private final T result;
    private final Throwable exception;
    private final long executionTimeMs;
    private final Instant timestamp;
    
    private TaskResult(boolean success, T result, Throwable exception, long executionTimeMs) {
        this.success = success;
        this.result = result;
        this.exception = exception;
        this.executionTimeMs = executionTimeMs;
        this.timestamp = Instant.now();
    }
    
    /**
     * Creates a successful task result
     * 
     * @param result the result data
     * @param executionTimeMs execution time in milliseconds
     * @param <T> result type
     * @return successful TaskResult
     */
    public static <T> TaskResult<T> success(T result, long executionTimeMs) {
        return new TaskResult<>(true, result, null, executionTimeMs);
    }
    
    /**
     * Creates a failed task result
     * 
     * @param exception the exception that caused failure
     * @param executionTimeMs execution time in milliseconds
     * @param <T> result type
     * @return failed TaskResult
     */
    public static <T> TaskResult<T> failure(Throwable exception, long executionTimeMs) {
        return new TaskResult<>(false, null, exception, executionTimeMs);
    }
    
    /**
     * Checks if the task was successful
     * 
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Gets the result data
     * 
     * @return result data or null if failed
     */
    public T getResult() {
        return result;
    }
    
    /**
     * Gets the exception if task failed
     * 
     * @return exception or null if successful
     */
    public Throwable getException() {
        return exception;
    }
    
    /**
     * Gets the execution time in milliseconds
     * 
     * @return execution time
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    /**
     * Gets the timestamp when result was created
     * 
     * @return timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "TaskResult{" +
                "success=" + success +
                ", result=" + result +
                ", exception=" + exception +
                ", executionTimeMs=" + executionTimeMs +
                ", timestamp=" + timestamp +
                '}';
    }
}