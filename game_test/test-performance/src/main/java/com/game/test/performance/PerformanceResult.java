package com.game.test.performance;

/**
 * 性能测试结果
 * @author lx
 * @date 2025/06/08
 */
public class PerformanceResult {
    
    private String testName;
    private int totalRequests;
    private int successfulRequests;
    private int failedRequests;
    private long totalDuration; // milliseconds
    private long averageResponseTime; // milliseconds
    private double throughput; // requests per second
    private double successRate;
    
    public static PerformanceResult.Builder builder() {
        return new PerformanceResult.Builder();
    }
    
    public static class Builder {
        private PerformanceResult result = new PerformanceResult();
        
        public Builder testName(String testName) {
            result.testName = testName;
            return this;
        }
        
        public Builder totalRequests(int totalRequests) {
            result.totalRequests = totalRequests;
            return this;
        }
        
        public Builder successfulRequests(int successfulRequests) {
            result.successfulRequests = successfulRequests;
            return this;
        }
        
        public Builder failedRequests(int failedRequests) {
            result.failedRequests = failedRequests;
            return this;
        }
        
        public Builder totalDuration(long totalDuration) {
            result.totalDuration = totalDuration;
            return this;
        }
        
        public Builder averageResponseTime(long averageResponseTime) {
            result.averageResponseTime = averageResponseTime;
            return this;
        }
        
        public Builder throughput(double throughput) {
            result.throughput = throughput;
            return this;
        }
        
        public PerformanceResult build() {
            // Calculate success rate
            if (result.totalRequests > 0) {
                result.successRate = (double) result.successfulRequests / result.totalRequests * 100;
            }
            return result;
        }
    }
    
    // Getters
    public String getTestName() { return testName; }
    public int getTotalRequests() { return totalRequests; }
    public int getSuccessfulRequests() { return successfulRequests; }
    public int getFailedRequests() { return failedRequests; }
    public long getTotalDuration() { return totalDuration; }
    public long getAverageResponseTime() { return averageResponseTime; }
    public double getThroughput() { return throughput; }
    public double getSuccessRate() { return successRate; }
    
    @Override
    public String toString() {
        return String.format(
            "PerformanceResult{testName='%s', total=%d, success=%d, failed=%d, duration=%dms, avgResponse=%dms, throughput=%.2f/s, successRate=%.2f%%}",
            testName, totalRequests, successfulRequests, failedRequests, totalDuration, averageResponseTime, throughput, successRate
        );
    }
}