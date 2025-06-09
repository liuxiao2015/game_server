package com.game.test;

import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 测试框架核心
 * @author lx
 * @date 2025/06/08
 */
public class TestFramework {
    
    private static final Logger logger = LoggerFactory.getLogger(TestFramework.class);
    
    // Test containers
    private static MySQLContainer<?> mysqlContainer;
    private static GenericContainer<?> redisContainer;
    
    // Test data cache
    private static final Map<String, Object> testDataCache = new ConcurrentHashMap<>();
    
    /**
     * 测试环境初始化
     */
    @Step("初始化测试环境")
    public static void setupTestEnvironment() {
        logger.info("Initializing test environment...");
        
        // Start MySQL container
        if (mysqlContainer == null || !mysqlContainer.isRunning()) {
            mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("game_test")
                    .withUsername("test")
                    .withPassword("test123")
                    .withInitScript("init-test-db.sql");
            mysqlContainer.start();
            logger.info("MySQL container started: {}", mysqlContainer.getJdbcUrl());
        }
        
        // Start Redis container
        if (redisContainer == null || !redisContainer.isRunning()) {
            redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);
            redisContainer.start();
            logger.info("Redis container started on port: {}", redisContainer.getMappedPort(6379));
        }
        
        logger.info("Test environment initialized successfully");
    }
    
    /**
     * 测试数据准备
     */
    @Step("准备测试数据")
    public static void prepareTestData() {
        logger.info("Preparing test data...");
        
        // Clear existing test data
        testDataCache.clear();
        
        // Prepare common test data
        TestDataFactory.createTestUsers();
        TestDataFactory.createTestItems();
        TestDataFactory.createTestOrders();
        
        logger.info("Test data prepared successfully");
    }
    
    /**
     * 测试报告生成
     */
    @Step("生成测试报告")
    public static TestReport generateReport() {
        logger.info("Generating test report...");
        
        TestReport report = TestReport.builder()
                .testSuiteName("Game Server Test Suite")
                .totalTests(getTotalTestCount())
                .passedTests(getPassedTestCount())
                .failedTests(getFailedTestCount())
                .skippedTests(getSkippedTestCount())
                .executionTime(getExecutionTime())
                .build();
        
        logger.info("Test report generated: {}", report);
        return report;
    }
    
    /**
     * 清理测试环境
     */
    @Step("清理测试环境")
    public static void cleanupTestEnvironment() {
        logger.info("Cleaning up test environment...");
        
        // Clear test data
        testDataCache.clear();
        
        // Stop containers if needed (usually handled by Testcontainers)
        logger.info("Test environment cleaned up");
    }
    
    /**
     * 配置动态属性
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (mysqlContainer != null && mysqlContainer.isRunning()) {
            registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
            registry.add("spring.datasource.username", mysqlContainer::getUsername);
            registry.add("spring.datasource.password", mysqlContainer::getPassword);
        }
        
        if (redisContainer != null && redisContainer.isRunning()) {
            registry.add("spring.redis.host", redisContainer::getHost);
            registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379).toString());
        }
    }
    
    // Helper methods for test metrics
    private static int getTotalTestCount() {
        // TODO: 实现获取总测试数量
        return 100;
    }
    
    private static int getPassedTestCount() {
        // TODO: 实现获取通过测试数量
        return 85;
    }
    
    private static int getFailedTestCount() {
        // TODO: 实现获取失败测试数量
        return 10;
    }
    
    private static int getSkippedTestCount() {
        // TODO: 实现获取跳过测试数量
        return 5;
    }
    
    private static long getExecutionTime() {
        // TODO: 实现获取执行时间
        return 120000; // 2 minutes
    }
    
    // Getters for test containers
    public static MySQLContainer<?> getMysqlContainer() {
        return mysqlContainer;
    }
    
    public static GenericContainer<?> getRedisContainer() {
        return redisContainer;
    }
    
    public static Map<String, Object> getTestDataCache() {
        return testDataCache;
    }
}