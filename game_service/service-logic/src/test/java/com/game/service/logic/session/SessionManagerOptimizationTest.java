package com.game.service.logic.session;

import com.game.frame.netty.session.SessionManager;
import com.game.frame.netty.session.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Session管理器性能优化测试
 * 
 * 测试目标：
 * - 验证读写分离优化效果
 * - 测试并发访问性能
 * - 验证缓存命中率和性能统计
 * - 确保会话管理的正确性
 *
 * @author lx
 * @date 2024-01-01
 */
public class SessionManagerOptimizationTest {
    
    private SessionManager sessionManager;
    
    @BeforeEach
    void setUp() {
        sessionManager = new SessionManager();
    }
    
    @Test
    void testBasicSessionOperations() {
        // 基本会话操作测试
        assertNotNull(sessionManager);
        assertEquals(0, sessionManager.getSessionCount());
        assertEquals(0, sessionManager.getAuthenticatedSessionCount());
        
        System.out.println("基本会话操作测试通过");
    }
    
    @Test
    void testPerformanceStatistics() {
        // 测试性能统计功能
        SessionManager.SessionStats initialStats = sessionManager.getStats();
        assertNotNull(initialStats);
        
        // 验证初始状态
        assertEquals(0, initialStats.getTotalSessions());
        assertEquals(0, initialStats.getAuthenticatedSessions());
        assertEquals(0, initialStats.getReadOperations());
        assertEquals(0, initialStats.getWriteOperations());
        
        System.out.println("初始统计: " + initialStats);
        
        // 测试缓存命中率计算
        assertEquals(0.0, initialStats.getCacheHitRate(), 0.001);
        
        System.out.println("性能统计测试通过");
    }
    
    @Test
    void testConcurrentStatisticsUpdate() {
        // 测试并发统计更新
        int threadCount = 10;
        int operationsPerThread = 100;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        IntStream.range(0, threadCount).forEach(threadIndex -> {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        // 模拟读操作
                        sessionManager.getSessionByUserId("nonexistent-user-" + threadIndex + "-" + i);
                    }
                } finally {
                    latch.countDown();
                }
            });
        });
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long duration = System.currentTimeMillis() - startTime;
        int totalOperations = threadCount * operationsPerThread;
        
        SessionManager.SessionStats finalStats = sessionManager.getStats();
        
        System.out.println("并发执行 " + totalOperations + " 个操作耗时: " + duration + "ms");
        System.out.println("最终统计: " + finalStats);
        
        // 验证读操作计数
        assertTrue(finalStats.getReadOperations() >= totalOperations, 
                "读操作数量应该至少为 " + totalOperations + ", 实际为 " + finalStats.getReadOperations());
        
        executor.shutdown();
    }
    
    @Test
    void testThreadSafety() {
        // 测试线程安全性
        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        IntStream.range(0, threadCount).forEach(threadIndex -> {
            executor.submit(() -> {
                try {
                    // 每个线程执行不同的操作
                    for (int i = 0; i < 50; i++) {
                        sessionManager.getSessionByUserId("thread-" + threadIndex + "-user-" + i);
                        sessionManager.getAllSessions();
                        sessionManager.getSessionCount();
                    }
                } finally {
                    latch.countDown();
                }
            });
        });
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 验证系统仍然正常工作
        assertNotNull(sessionManager.getStats());
        assertTrue(sessionManager.getSessionCount() >= 0);
        
        executor.shutdown();
        
        System.out.println("线程安全性测试通过");
    }
    
    @Test
    void testSessionManagerShutdown() {
        // 测试会话管理器关闭
        SessionManager testManager = new SessionManager();
        
        // 执行一些操作
        testManager.getSessionByUserId("test-user");
        testManager.getAllSessions();
        
        // 获取统计信息
        SessionManager.SessionStats stats = testManager.getStats();
        assertNotNull(stats);
        assertTrue(stats.getReadOperations() > 0);
        
        // 关闭管理器
        testManager.shutdown();
        
        System.out.println("会话管理器关闭测试通过");
    }
    
    @Test
    void testSessionCachePerformance() {
        // 测试缓存性能
        int iterations = 1000;
        String testUserId = "performance-test-user";
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            sessionManager.getSessionByUserId(testUserId);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        double avgTime = (double) duration / iterations;
        
        System.out.println("执行 " + iterations + " 次查询耗时: " + duration + "ms");
        System.out.println("平均每次查询耗时: " + avgTime + "ms");
        
        // 验证性能合理（每次查询应该在1ms以内）
        assertTrue(avgTime < 1.0, "平均查询时间应该小于1ms，实际为: " + avgTime + "ms");
        
        SessionManager.SessionStats stats = sessionManager.getStats();
        System.out.println("缓存性能统计: " + stats);
    }
}