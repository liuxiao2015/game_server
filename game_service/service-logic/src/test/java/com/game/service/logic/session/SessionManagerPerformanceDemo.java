package com.game.service.logic.session;

/**
 * 简单的性能基准测试，展示Session管理优化效果
 * 
 * 测试内容：
 * - 基本操作性能验证
 * - 读写分离效果验证
 * - 统计功能验证
 *
 * @author lx
 * @date 2024-01-01
 */
public class SessionManagerPerformanceDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Session管理器性能优化演示 ===\n");
        
        // 创建优化后的SessionManager
        com.game.frame.netty.session.SessionManager optimizedManager = 
            new com.game.frame.netty.session.SessionManager();
        
        demonstrateBasicOperations(optimizedManager);
        demonstrateConcurrencyPerformance(optimizedManager);
        demonstrateStatistics(optimizedManager);
        
        // 关闭管理器
        optimizedManager.shutdown();
        
        System.out.println("\n=== 演示完成 ===");
    }
    
    private static void demonstrateBasicOperations(com.game.frame.netty.session.SessionManager manager) {
        System.out.println("1. 基本操作演示:");
        
        // 获取初始状态
        System.out.println("   初始会话数: " + manager.getSessionCount());
        System.out.println("   初始认证会话数: " + manager.getAuthenticatedSessionCount());
        
        // 模拟一些查询操作
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            manager.getSessionByUserId("nonexistent-user-" + i);
        }
        long duration = System.nanoTime() - startTime;
        
        System.out.println("   执行1000次查询耗时: " + (duration / 1_000_000) + "ms");
        
        // 获取统计信息
        com.game.frame.netty.session.SessionManager.SessionStats stats = manager.getStats();
        System.out.println("   读操作数: " + stats.getReadOperations());
        System.out.println("   缓存未命中数: " + stats.getCacheMisses());
        System.out.println("");
    }
    
    private static void demonstrateConcurrencyPerformance(com.game.frame.netty.session.SessionManager manager) {
        System.out.println("2. 并发性能演示:");
        
        int threadCount = 10;
        int operationsPerThread = 500;
        
        Thread[] threads = new Thread[threadCount];
        long startTime = System.currentTimeMillis();
        
        // 创建并启动线程
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    manager.getSessionByUserId("thread-" + threadIndex + "-user-" + j);
                    manager.getAllSessions();
                    manager.getSessionCount();
                }
            });
            threads[i].start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        int totalOperations = threadCount * operationsPerThread * 3; // 每轮3个操作
        
        System.out.println("   " + threadCount + "个线程并发执行" + totalOperations + "个操作");
        System.out.println("   总耗时: " + duration + "ms");
        System.out.println("   平均TPS: " + (totalOperations * 1000 / duration));
        System.out.println("");
    }
    
    private static void demonstrateStatistics(com.game.frame.netty.session.SessionManager manager) {
        System.out.println("3. 性能统计演示:");
        
        com.game.frame.netty.session.SessionManager.SessionStats stats = manager.getStats();
        
        System.out.println("   总会话数: " + stats.getTotalSessions());
        System.out.println("   认证会话数: " + stats.getAuthenticatedSessions());
        System.out.println("   读操作数: " + stats.getReadOperations());
        System.out.println("   写操作数: " + stats.getWriteOperations());
        System.out.println("   缓存命中数: " + stats.getCacheHits());
        System.out.println("   缓存未命中数: " + stats.getCacheMisses());
        System.out.println("   缓存命中率: " + String.format("%.2f%%", stats.getCacheHitRate() * 100));
        System.out.println("   完整统计: " + stats);
        System.out.println("");
    }
}