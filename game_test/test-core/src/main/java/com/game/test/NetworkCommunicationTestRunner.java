package com.game.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * 网络通信测试执行器
 * 
 * 功能说明：
 * - 执行网络通信综合测试
 * - 输出详细的测试报告
 * - 演示测试框架的使用方法
 * 
 * 运行方式：
 * - 可以作为独立的Java应用程序运行
 * - 也可以集成到自动化测试框架中
 * 
 * 注意事项：
 * - 需要确保游戏服务器在localhost:8888运行
 * - 测试过程中会创建大量并发连接
 * 
 * @author lx
 * @date 2025/06/08
 */
public class NetworkCommunicationTestRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(NetworkCommunicationTestRunner.class);
    
    public static void main(String[] args) {
        logger.info("启动网络通信测试执行器...");
        
        NetworkCommunicationTest test = new NetworkCommunicationTest();
        
        try {
            // 执行测试
            CompletableFuture<TestReport> future = test.runAllTests();
            TestReport report = future.get();
            
            // 输出测试报告
            printTestReport(report, test);
            
            // 确定退出状态
            if (report.getSuccessRate() >= 80.0) {
                logger.info("测试通过：成功率 {:.2f}% >= 80%", report.getSuccessRate());
                System.exit(0);
            } else {
                logger.error("测试失败：成功率 {:.2f}% < 80%", report.getSuccessRate());
                System.exit(1);
            }
            
        } catch (Exception e) {
            logger.error("测试执行异常", e);
            System.exit(2);
        } finally {
            test.shutdown();
        }
    }
    
    /**
     * 打印详细的测试报告
     */
    private static void printTestReport(TestReport report, NetworkCommunicationTest test) {
        System.out.println();
        System.out.println("=========================================================");
        System.out.println("                网络通信测试报告");
        System.out.println("=========================================================");
        System.out.println();
        
        // 基本信息
        System.out.println("测试套件: " + report.getTestSuiteName());
        System.out.println("执行时间: " + report.getExecutionTime() + " ms");
        System.out.println();
        
        // 统计信息
        System.out.println("测试统计:");
        System.out.println("  总测试数: " + report.getTotalTests());
        System.out.println("  通过测试: " + report.getPassedTests());
        System.out.println("  失败测试: " + report.getFailedTests());
        System.out.println("  跳过测试: " + report.getSkippedTests());
        System.out.printf("  成功率: %.2f%%\n", report.getSuccessRate());
        System.out.println();
        
        // 详细结果
        System.out.println("详细结果:");
        for (String result : test.getTestResults()) {
            System.out.println("  " + result);
        }
        
        // 错误信息
        if (!test.getTestErrors().isEmpty()) {
            System.out.println();
            System.out.println("错误信息:");
            for (String error : test.getTestErrors()) {
                System.out.println("  " + error);
            }
        }
        
        System.out.println();
        System.out.println("=========================================================");
        
        // 测试建议
        if (report.getSuccessRate() >= 95.0) {
            System.out.println("✅ 优秀：网络通信功能表现出色！");
        } else if (report.getSuccessRate() >= 80.0) {
            System.out.println("✅ 良好：网络通信功能基本正常，建议优化部分功能。");
        } else if (report.getSuccessRate() >= 60.0) {
            System.out.println("⚠️ 警告：网络通信功能存在问题，需要重点关注。");
        } else {
            System.out.println("❌ 严重：网络通信功能异常，需要立即修复。");
        }
        
        System.out.println("=========================================================");
    }
}