package com.game.test.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能测试套件
 * @author lx
 * @date 2025/06/08
 */
public class PerformanceTestSuite {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestSuite.class);
    
    private final ExecutorService executorService;
    private final PerformanceMonitor monitor;
    
    public PerformanceTestSuite() {
        this.executorService = Executors.newCachedThreadPool();
        this.monitor = new PerformanceMonitor();
    }
    
    /**
     * 并发登录测试
     */
    public CompletableFuture<PerformanceResult> testConcurrentLogin(int users) {
        logger.info("Starting concurrent login test with {} users", users);
        
        CompletableFuture<PerformanceResult> future = new CompletableFuture<>();
        
        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        
        CompletableFuture<Void>[] tasks = new CompletableFuture[users];
        
        for (int i = 0; i < users; i++) {
            final int userId = i;
            tasks[i] = CompletableFuture.runAsync(() -> {
                long requestStart = System.nanoTime();
                
                try {
                    // 模拟登录请求
                    simulateLogin(userId);
                    
                    long responseTime = (System.nanoTime() - requestStart) / 1_000_000; // Convert to ms
                    totalResponseTime.addAndGet(responseTime);
                    successCount.incrementAndGet();
                    
                } catch (Exception e) {
                    logger.error("Login failed for user {}", userId, e);
                    failureCount.incrementAndGet();
                }
            }, executorService);
        }
        
        CompletableFuture.allOf(tasks).whenComplete((result, throwable) -> {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            PerformanceResult performanceResult = PerformanceResult.builder()
                    .testName("Concurrent Login Test")
                    .totalRequests(users)
                    .successfulRequests(successCount.get())
                    .failedRequests(failureCount.get())
                    .totalDuration(duration)
                    .averageResponseTime(successCount.get() > 0 ? totalResponseTime.get() / successCount.get() : 0)
                    .throughput(calculateThroughput(successCount.get(), duration))
                    .build();
            
            logger.info("Concurrent login test completed: {}", performanceResult);
            future.complete(performanceResult);
        });
        
        return future;
    }
    
    /**
     * 消息吞吐测试
     */
    public CompletableFuture<PerformanceResult> testMessageThroughput() {
        logger.info("Starting message throughput test");
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            int messageCount = 10000;
            AtomicInteger processedMessages = new AtomicInteger(0);
            
            for (int i = 0; i < messageCount; i++) {
                // 模拟消息处理
                simulateMessageProcessing();
                processedMessages.incrementAndGet();
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            return PerformanceResult.builder()
                    .testName("Message Throughput Test")
                    .totalRequests(messageCount)
                    .successfulRequests(processedMessages.get())
                    .failedRequests(0)
                    .totalDuration(duration)
                    .averageResponseTime(duration / messageCount)
                    .throughput(calculateThroughput(processedMessages.get(), duration))
                    .build();
        }, executorService);
    }
    
    /**
     * 数据库压力测试
     */
    public CompletableFuture<PerformanceResult> testDatabasePerformance() {
        logger.info("Starting database performance test");
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            int operations = 1000;
            AtomicInteger successCount = new AtomicInteger(0);
            
            for (int i = 0; i < operations; i++) {
                try {
                    // 模拟数据库操作
                    simulateDatabaseOperation();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    logger.error("Database operation failed", e);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            return PerformanceResult.builder()
                    .testName("Database Performance Test")
                    .totalRequests(operations)
                    .successfulRequests(successCount.get())
                    .failedRequests(operations - successCount.get())
                    .totalDuration(duration)
                    .averageResponseTime(duration / operations)
                    .throughput(calculateThroughput(successCount.get(), duration))
                    .build();
        }, executorService);
    }
    
    /**
     * 缓存性能测试
     */
    public CompletableFuture<PerformanceResult> testCachePerformance() {
        logger.info("Starting cache performance test");
        
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            int operations = 10000;
            AtomicInteger successCount = new AtomicInteger(0);
            
            for (int i = 0; i < operations; i++) {
                try {
                    // 模拟缓存操作
                    simulateCacheOperation();
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    logger.error("Cache operation failed", e);
                }
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            return PerformanceResult.builder()
                    .testName("Cache Performance Test")
                    .totalRequests(operations)
                    .successfulRequests(successCount.get())
                    .failedRequests(operations - successCount.get())
                    .totalDuration(duration)
                    .averageResponseTime(duration / operations)
                    .throughput(calculateThroughput(successCount.get(), duration))
                    .build();
        }, executorService);
    }
    
    // 模拟方法
    private void simulateLogin(int userId) throws InterruptedException {
        // 模拟登录处理时间
        Thread.sleep(50 + (int)(Math.random() * 100));
    }
    
    private void simulateMessageProcessing() {
        // 模拟消息处理
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private void simulateDatabaseOperation() throws InterruptedException {
        // 模拟数据库操作时间
        Thread.sleep(10 + (int)(Math.random() * 50));
    }
    
    private void simulateCacheOperation() {
        // 模拟缓存操作（很快）
        Math.random(); // 简单的计算模拟
    }
    
    private double calculateThroughput(int requests, long durationMs) {
        if (durationMs == 0) return 0;
        return (double) requests / durationMs * 1000; // requests per second
    }
    
    /**
     * 关闭执行器
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}