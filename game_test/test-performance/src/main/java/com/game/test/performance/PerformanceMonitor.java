package com.game.test.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控
 * @author lx
 * @date 2025/06/08
 */
public class PerformanceMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    private final AtomicLong requestCount = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private volatile long startTime = System.currentTimeMillis();
    
    /**
     * 记录请求
     */
    public void recordRequest(long responseTimeMs, boolean success) {
        requestCount.incrementAndGet();
        totalResponseTime.addAndGet(responseTimeMs);
        
        if (!success) {
            errorCount.incrementAndGet();
        }
    }
    
    /**
     * 获取当前QPS
     */
    public double getCurrentQPS() {
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - startTime) / 1000;
        
        if (elapsedSeconds == 0) {
            return 0;
        }
        
        return (double) requestCount.get() / elapsedSeconds;
    }
    
    /**
     * 获取平均响应时间
     */
    public double getAverageResponseTime() {
        long count = requestCount.get();
        if (count == 0) {
            return 0;
        }
        
        return (double) totalResponseTime.get() / count;
    }
    
    /**
     * 获取错误率
     */
    public double getErrorRate() {
        long count = requestCount.get();
        if (count == 0) {
            return 0;
        }
        
        return (double) errorCount.get() / count * 100;
    }
    
    /**
     * 重置计数器
     */
    public void reset() {
        requestCount.set(0);
        totalResponseTime.set(0);
        errorCount.set(0);
        startTime = System.currentTimeMillis();
    }
    
    /**
     * 打印当前统计信息
     */
    public void printStats() {
        logger.info("Performance Stats - QPS: {}, Avg Response: {}ms, Error Rate: {}%", 
                    String.format("%.2f", getCurrentQPS()),
                    String.format("%.2f", getAverageResponseTime()),
                    String.format("%.2f", getErrorRate()));
    }
    
    // Getters
    public long getRequestCount() { return requestCount.get(); }
    public long getTotalResponseTime() { return totalResponseTime.get(); }
    public long getErrorCount() { return errorCount.get(); }
    public long getStartTime() { return startTime; }
}