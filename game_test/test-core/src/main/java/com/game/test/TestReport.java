package com.game.test;

/**
 * 游戏测试报告类
 * 
 * 功能说明：
 * - 封装自动化测试执行结果的完整信息
 * - 提供测试统计数据的标准化格式
 * - 支持测试报告的生成和导出功能
 * - 为测试结果分析提供数据基础
 * 
 * 设计思路：
 * - 使用构建器模式创建复杂的报告对象
 * - 提供完整的测试指标和统计信息
 * - 支持测试结果的结构化存储
 * - 便于集成到CI/CD流水线中
 * 
 * 报告内容：
 * - 基本信息：测试套件名称、执行时间
 * - 统计数据：总数、通过、失败、跳过的测试数量
 * - 成功率：自动计算的测试通过率
 * - 详细信息：可扩展的测试详情和错误信息
 * 
 * 统计指标：
 * - 总测试数：包含所有执行的测试用例
 * - 通过测试数：成功执行的测试用例数量
 * - 失败测试数：执行失败的测试用例数量
 * - 跳过测试数：被跳过未执行的测试用例数量
 * - 执行时间：测试套件的总执行时长
 * - 成功率：通过测试数除以总测试数的百分比
 * 
 * 应用场景：
 * - 持续集成中的测试结果汇总
 * - 测试质量的监控和分析
 * - 测试报告的自动化生成
 * - 项目质量评估的数据支撑
 * 
 * 扩展能力：
 * - 支持添加更多测试维度指标
 * - 支持不同格式的报告导出
 * - 支持测试历史数据的对比分析
 * - 支持测试覆盖率等高级指标
 *
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