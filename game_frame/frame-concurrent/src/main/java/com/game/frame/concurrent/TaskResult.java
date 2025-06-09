package com.game.frame.concurrent;

import java.time.Instant;

/**
 * 任务执行结果包装类
 * 
 * 功能说明：
 * - 封装任务执行的成功/失败状态
 * - 包含结果数据、异常信息和执行时间等完整信息
 * - 提供统一的结果处理接口
 * 
 * 设计思路：
 * - 使用泛型支持各种返回类型
 * - 不可变对象设计，确保线程安全
 * - 同时记录成功和失败的详细信息
 * - 包含时间戳便于问题追踪和性能分析
 * 
 * 使用场景：
 * - 异步任务执行结果的统一返回格式
 * - 需要详细执行信息的业务场景
 * - 性能监控和错误追踪
 *
 * @param <T> 任务返回结果的数据类型
 * @author lx
 * @date 2024-01-01
 */
public class TaskResult<T> {
    
    // 任务执行是否成功的标志
    private final boolean success;
    // 任务执行成功时的结果数据（失败时为null）
    private final T result;
    // 任务执行失败时的异常信息（成功时为null）
    private final Throwable exception;
    // 任务执行耗时（毫秒）
    private final long executionTimeMs;
    // 结果创建的时间戳，用于追踪和调试
    private final Instant timestamp;
    
    /**
     * 私有构造函数，防止外部直接实例化
     * 通过静态工厂方法创建实例，确保数据一致性
     * 
     * @param success 执行是否成功
     * @param result 成功时的结果数据
     * @param exception 失败时的异常信息
     * @param executionTimeMs 执行耗时
     */
    private TaskResult(boolean success, T result, Throwable exception, long executionTimeMs) {
        this.success = success;
        this.result = result;
        this.exception = exception;
        this.executionTimeMs = executionTimeMs;
        this.timestamp = Instant.now(); // 记录创建时间
    }
    
    /**
     * 创建成功的任务结果
     * 
     * @param result 任务执行的结果数据
     * @param executionTimeMs 任务执行耗时（毫秒）
     * @param <T> 结果数据类型
     * @return 包含成功信息的TaskResult实例
     */
    public static <T> TaskResult<T> success(T result, long executionTimeMs) {
        return new TaskResult<>(true, result, null, executionTimeMs);
    }
    
    /**
     * 创建失败的任务结果
     * 
     * @param exception 导致失败的异常信息
     * @param executionTimeMs 任务执行耗时（毫秒）
     * @param <T> 结果数据类型
     * @return 包含失败信息的TaskResult实例
     */
    public static <T> TaskResult<T> failure(Throwable exception, long executionTimeMs) {
        return new TaskResult<>(false, null, exception, executionTimeMs);
    }
    
    /**
     * 检查任务是否执行成功
     * 
     * @return true表示执行成功，false表示执行失败
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * 获取任务执行的结果数据
     * 
     * @return 成功时返回结果数据，失败时返回null
     */
    public T getResult() {
        return result;
    }
    
    /**
     * 获取任务执行失败时的异常信息
     * 
     * @return 失败时返回异常对象，成功时返回null
     */
    public Throwable getException() {
        return exception;
    }
    
    /**
     * 获取任务执行耗时
     * 
     * @return 执行耗时（毫秒）
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    /**
     * 获取结果创建时的时间戳
     * 
     * @return 时间戳，用于追踪和调试
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "TaskResult{" +
                "success=" + success +
                ", result=" + result +
                ", exception=" + exception +
                ", executionTimeMs=" + executionTimeMs +
                ", timestamp=" + timestamp +
                '}';
    }
}