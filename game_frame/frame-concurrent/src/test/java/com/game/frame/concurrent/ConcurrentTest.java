package com.game.frame.concurrent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Test for concurrent utilities
 *
 * @author lx
 * @date 2024-01-01
 */
public class ConcurrentTest {
    
    private VirtualThreadExecutor executor;
    
    @BeforeEach
    public void setUp() {
        executor = new VirtualThreadExecutor("Test");
    }
    
    @AfterEach
    public void tearDown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    @Test
    public void testVirtualThreadExecutor() throws InterruptedException {
        // Test basic task submission
        Runnable task = () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        
        executor.submit(task);
        
        // Wait a bit for task to complete
        Thread.sleep(200);
        
        assertTrue(executor.getSubmittedTaskCount() > 0);
        assertTrue(executor.getCompletedTaskCount() > 0);
        assertEquals(0, executor.getRejectedTaskCount());
    }
    
    @Test
    public void testStructuredTaskManager() {
        List<Callable<String>> tasks = List.of(
            () -> "Task 1",
            () -> "Task 2",
            () -> "Task 3"
        );
        
        List<TaskResult<String>> results = StructuredTaskManager.executeAll(tasks, Duration.ofSeconds(5));
        
        assertEquals(3, results.size());
        for (TaskResult<String> result : results) {
            assertTrue(result.isSuccess());
            assertNotNull(result.getResult());
            assertTrue(result.getResult().startsWith("Task"));
        }
    }
    
    @Test
    public void testConcurrentUtils() {
        Callable<String> task = () -> "Success";
        
        TaskResult<String> result = ConcurrentUtils.executeWithRetry(task);
        
        assertTrue(result.isSuccess());
        assertEquals("Success", result.getResult());
        assertNull(result.getException());
    }
    
    @Test
    public void testTaskResult() {
        TaskResult<String> successResult = TaskResult.success("test", 100);
        assertTrue(successResult.isSuccess());
        assertEquals("test", successResult.getResult());
        assertEquals(100, successResult.getExecutionTimeMs());
        assertNull(successResult.getException());
        
        Exception ex = new RuntimeException("test error");
        TaskResult<String> failureResult = TaskResult.failure(ex, 50);
        assertFalse(failureResult.isSuccess());
        assertNull(failureResult.getResult());
        assertEquals(50, failureResult.getExecutionTimeMs());
        assertEquals(ex, failureResult.getException());
    }
}