package com.game.test;

/**
 * 测试报告
 * @author lx
 * @date 2025/06/08
 */
public class TestReport {
    
    private String testSuiteName;
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private int skippedTests;
    private long executionTime;
    private double successRate;
    
    public static TestReport.Builder builder() {
        return new TestReport.Builder();
    }
    
    public static class Builder {
        private TestReport report = new TestReport();
        
        public Builder testSuiteName(String testSuiteName) {
            report.testSuiteName = testSuiteName;
            return this;
        }
        
        public Builder totalTests(int totalTests) {
            report.totalTests = totalTests;
            return this;
        }
        
        public Builder passedTests(int passedTests) {
            report.passedTests = passedTests;
            return this;
        }
        
        public Builder failedTests(int failedTests) {
            report.failedTests = failedTests;
            return this;
        }
        
        public Builder skippedTests(int skippedTests) {
            report.skippedTests = skippedTests;
            return this;
        }
        
        public Builder executionTime(long executionTime) {
            report.executionTime = executionTime;
            return this;
        }
        
        public TestReport build() {
            // Calculate success rate
            if (report.totalTests > 0) {
                report.successRate = (double) report.passedTests / report.totalTests * 100;
            }
            return report;
        }
    }
    
    // Getters
    public String getTestSuiteName() { return testSuiteName; }
    public int getTotalTests() { return totalTests; }
    public int getPassedTests() { return passedTests; }
    public int getFailedTests() { return failedTests; }
    public int getSkippedTests() { return skippedTests; }
    public long getExecutionTime() { return executionTime; }
    public double getSuccessRate() { return successRate; }
    
    @Override
    public String toString() {
        return String.format(
            "TestReport{testSuiteName='%s', total=%d, passed=%d, failed=%d, skipped=%d, successRate=%.2f%%, executionTime=%dms}",
            testSuiteName, totalTests, passedTests, failedTests, skippedTests, successRate, executionTime
        );
    }
}