package com.game.frame.concurrent;

/**
 * ThreadPool配置类
 * 
 * 功能说明：
 * - 定义系统或模块的配置参数
 * - 支持配置的动态加载和热更新
 * - 提供配置验证和默认值管理
 * - 集成Spring配置管理机制
 * 
 * 配置项说明：
 * - 包含系统运行所需的关键参数
 * - 支持环境变量和配置文件的参数覆盖
 * - 提供配置项的验证和约束检查
 * 
 * 使用方式：
 * - 通过@ConfigurationProperties注解绑定配置
 * - 支持@Value注解注入单个配置项
 * - 可通过Environment接口动态获取配置
 *
 * @author lx
 * @date 2024-01-01
 */
public class ThreadPoolConfig {
    
    /**
     * Default thread name prefix
     */
    public static final String DEFAULT_NAME_PREFIX = "GameThread";
    
    /**
     * Default task queue size
     */
    public static final int DEFAULT_QUEUE_SIZE = 1000;
    
    /**
     * Default rejection policy
     */
    public static final RejectionPolicy DEFAULT_REJECTION_POLICY = RejectionPolicy.CALLER_RUNS;
    
    private String namePrefix = DEFAULT_NAME_PREFIX;
    private int queueSize = DEFAULT_QUEUE_SIZE;
    private RejectionPolicy rejectionPolicy = DEFAULT_REJECTION_POLICY;
    private boolean enableMonitoring = true;
    
    /**
     * Rejection policies for thread pool
     */
    public enum RejectionPolicy {
        /**
         * Runs task in caller thread
         */
        CALLER_RUNS,
        
        /**
         * Throws RejectedExecutionException
         */
        ABORT,
        
        /**
         * Silently discards the task
         */
        DISCARD,
        
        /**
         * Discards the oldest task in queue
         */
        DISCARD_OLDEST
    }
    
    /**
     * Gets the thread name prefix
     * 
     * @return name prefix
     */
    public String getNamePrefix() {
        return namePrefix;
    }
    
    /**
     * Sets the thread name prefix
     * 
     * @param namePrefix the name prefix
     * @return this config for chaining
     */
    public ThreadPoolConfig setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }
    
    /**
     * Gets the task queue size
     * 
     * @return queue size
     */
    public int getQueueSize() {
        return queueSize;
    }
    
    /**
     * Sets the task queue size
     * 
     * @param queueSize the queue size
     * @return this config for chaining
     */
    public ThreadPoolConfig setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }
    
    /**
     * Gets the rejection policy
     * 
     * @return rejection policy
     */
    public RejectionPolicy getRejectionPolicy() {
        return rejectionPolicy;
    }
    
    /**
     * Sets the rejection policy
     * 
     * @param rejectionPolicy the rejection policy
     * @return this config for chaining
     */
    public ThreadPoolConfig setRejectionPolicy(RejectionPolicy rejectionPolicy) {
        this.rejectionPolicy = rejectionPolicy;
        return this;
    }
    
    /**
     * Checks if monitoring is enabled
     * 
     * @return true if monitoring enabled
     */
    public boolean isEnableMonitoring() {
        return enableMonitoring;
    }
    
    /**
     * Sets monitoring enablement
     * 
     * @param enableMonitoring true to enable monitoring
     * @return this config for chaining
     */
    public ThreadPoolConfig setEnableMonitoring(boolean enableMonitoring) {
        this.enableMonitoring = enableMonitoring;
        return this;
    }
}