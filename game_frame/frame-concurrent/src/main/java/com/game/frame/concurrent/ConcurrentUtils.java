package com.game.frame.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 并发工具类
 * 
 * 功能说明：
 * - 提供并发任务执行的高级抽象和工具方法
 * - 实现批量任务处理、重试机制、超时控制等常用并发模式
 * - 集成结构化任务管理，提供统一的结果处理和异常管理
 * - 支持游戏服务器高并发场景下的任务编排和执行
 * 
 * 设计思路：
 * - 采用静态工具类设计，便于在各个业务模块中复用
 * - 基于TaskResult统一结果封装，提供一致的错误处理体验
 * - 支持可配置的重试策略和超时控制，适应不同业务场景
 * - 集成CompletableFuture实现高性能异步任务处理
 * 
 * 核心功能：
 * - 批量任务执行：支持分批处理大量任务，避免系统资源耗尽
 * - 智能重试机制：指数退避重试，提高任务成功率
 * - 超时控制：防止长时间运行的任务阻塞系统
 * - 线程中断处理：正确响应线程中断信号，确保优雅关闭
 * 
 * 使用场景：
 * - 游戏数据批量处理（批量保存玩家数据、批量发送邮件等）
 * - 不稳定网络环境下的远程服务调用重试
 * - 需要超时控制的计算密集型任务
 * - 多个异步任务的并发执行和结果聚合
 * 
 * 性能特点：
 * - 低内存占用：合理的批次大小控制，避免内存溢出
 * - 高并发支持：基于虚拟线程和CompletableFuture的现代并发模型
 * - 智能资源管理：自动清理和释放资源，防止内存泄漏
 *
 * @author lx
 * @date 2024-01-01
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConcurrentUtils {
    
    // 日志记录器，用于记录任务执行状态和异常信息
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentUtils.class);
    
    /**
     * 默认最大重试次数
     * 设置为3次重试，在网络抖动和临时性错误场景下能够有效恢复
     */
    public static final int MAX_RETRY_ATTEMPTS = 3;
    
    /**
     * 默认重试延迟时间（毫秒）
     * 初始延迟1秒，配合指数退避策略使用
     */
    public static final long DEFAULT_RETRY_DELAY_MS = 1000;
    
    /**
     * 默认批处理大小
     * 单批次处理100个任务，平衡内存使用和处理效率
     */
    public static final int DEFAULT_BATCH_SIZE = 100;
    
    /**
     * 最大批处理大小限制
     * 防止单批次任务过多导致内存溢出
     */
    public static final int MAX_BATCH_SIZE = 1000;
    
    /**
     * 批量执行任务
     * 
     * 功能说明：
     * - 将大量任务分批处理，避免系统资源耗尽和内存溢出
     * - 支持自定义批次大小和超时时间，适应不同业务场景
     * - 提供详细的执行统计信息，便于性能监控和问题定位
     * 
     * 执行流程：
     * 1. 验证输入参数的合法性（任务列表、批次大小、超时时间）
     * 2. 将任务列表按指定大小分批处理
     * 3. 每批任务并发执行，等待所有任务完成或超时
     * 4. 收集所有批次的执行结果
     * 5. 统计成功和失败的任务数量
     * 6. 记录详细的执行日志
     * 
     * 性能优化：
     * - 使用ArrayList预分配容量，减少数组扩容开销
     * - 分批处理避免创建过多线程，降低系统负载
     * - 及时释放中间结果，减少内存占用
     * 
     * 异常处理：
     * - 单个任务失败不影响其他任务执行
     * - 批次执行异常会被捕获并记录到结果中
     * - 提供完整的错误信息和执行时间统计
     * 
     * @param tasks 要执行的任务列表，不能为null或包含null元素
     * @param batchSize 每批次处理的任务数量，必须大于0且不超过MAX_BATCH_SIZE
     * @param timeout 每批次的超时时间，不能为null
     * @param <T> 任务返回值类型
     * @return 所有任务的执行结果列表，保持与输入任务列表相同的顺序
     * @throws IllegalArgumentException 当参数不合法时抛出
     * 
     * 使用示例：
     * <pre>
     * List&lt;Callable&lt;String&gt;&gt; tasks = Arrays.asList(
     *     () -&gt; "task1",
     *     () -&gt; "task2",
     *     () -&gt; "task3"
     * );
     * List&lt;TaskResult&lt;String&gt;&gt; results = ConcurrentUtils.executeBatch(
     *     tasks, 10, Duration.ofSeconds(30)
     * );
     * </pre>
     */
    public static <T> List<TaskResult<T>> executeBatch(List<Callable<T>> tasks, int batchSize, Duration timeout) {
        // 输入参数验证
        if (tasks == null) {
            throw new IllegalArgumentException("任务列表不能为null");
        }
        if (tasks.isEmpty()) {
            logger.debug("任务列表为空，返回空结果");
            return List.of();
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("批次大小必须大于0");
        }
        if (batchSize > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("批次大小不能超过" + MAX_BATCH_SIZE);
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("超时时间必须为正数");
        }
        
        // 检查任务列表中是否有null元素
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i) == null) {
                throw new IllegalArgumentException("任务列表中不能包含null元素，位置: " + i);
            }
        }
        
        logger.info("开始批量执行任务，总任务数: {}, 批次大小: {}, 超时时间: {}ms", 
                tasks.size(), batchSize, timeout.toMillis());
        
        // 预分配结果列表容量，减少扩容开销
        List<TaskResult<T>> allResults = new java.util.ArrayList<>(tasks.size());
        long totalStartTime = System.currentTimeMillis();
        int processedBatches = 0;
        
        try {
            // 分批处理任务
            for (int i = 0; i < tasks.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, tasks.size());
                List<Callable<T>> batch = tasks.subList(i, endIndex);
                
                long batchStartTime = System.currentTimeMillis();
                logger.debug("处理第{}批任务，任务范围: {}-{}", processedBatches + 1, i, endIndex - 1);
                
                try {
                    // 执行当前批次的任务
                    List<TaskResult<T>> batchResults = StructuredTaskManager.executeAll(batch, timeout);
                    allResults.addAll(batchResults);
                    
                    long batchExecutionTime = System.currentTimeMillis() - batchStartTime;
                    long batchSuccessCount = batchResults.stream()
                            .mapToLong(result -> result.isSuccess() ? 1 : 0)
                            .sum();
                    
                    logger.debug("第{}批任务完成，成功: {}, 失败: {}, 耗时: {}ms", 
                            processedBatches + 1, batchSuccessCount, 
                            batchResults.size() - batchSuccessCount, batchExecutionTime);
                    
                } catch (Exception e) {
                    // 批次执行异常，为当前批次的所有任务创建失败结果
                    logger.error("第{}批任务执行异常", processedBatches + 1, e);
                    long batchExecutionTime = System.currentTimeMillis() - batchStartTime;
                    
                    for (int j = 0; j < batch.size(); j++) {
                        allResults.add(TaskResult.failure(e, batchExecutionTime));
                    }
                }
                
                processedBatches++;
            }
            
            // 统计总体执行结果
            long totalExecutionTime = System.currentTimeMillis() - totalStartTime;
            long totalSuccessCount = allResults.stream()
                    .mapToLong(result -> result.isSuccess() ? 1 : 0)
                    .sum();
            long totalFailureCount = allResults.size() - totalSuccessCount;
            
            logger.info("批量任务执行完成，总耗时: {}ms, 总成功: {}, 总失败: {}, 成功率: {:.2f}%", 
                    totalExecutionTime, totalSuccessCount, totalFailureCount,
                    totalSuccessCount * 100.0 / allResults.size());
            
            return allResults;
            
        } catch (Exception e) {
            // 意外异常，记录错误并返回部分结果
            logger.error("批量任务执行过程中发生意外异常", e);
            
            // 如果已有部分结果，返回部分结果；否则返回空列表
            if (allResults.isEmpty()) {
                return List.of();
            }
            return allResults;
        }
    }
    
    /**
     * 带重试机制的任务执行
     * 
     * 功能说明：
     * - 实现智能重试策略，提高任务在不稳定环境下的成功率
     * - 支持指数退避延迟，避免重试风暴对系统造成压力
     * - 提供详细的重试过程日志，便于问题定位和性能分析
     * - 正确处理线程中断，支持优雅关闭
     * 
     * 重试策略：
     * - 指数退避：每次重试的延迟时间递增（基础延迟 * 2^重试次数）
     * - 最大延迟限制：单次延迟不超过30秒，防止长时间阻塞
     * - 异常分类：区分可重试异常和不可重试异常
     * - 中断响应：正确处理线程中断信号，立即停止重试
     * 
     * 执行流程：
     * 1. 验证输入参数（任务、重试次数、延迟时间）
     * 2. 循环执行任务，直到成功或达到最大重试次数
     * 3. 每次失败后计算下次重试的延迟时间（指数退避）
     * 4. 记录每次重试的详细信息
     * 5. 返回最终的执行结果和总耗时
     * 
     * 性能优化：
     * - 使用System.nanoTime()提供更精确的时间测量
     * - 合理的延迟算法，避免系统资源浪费
     * - 及时响应中断信号，避免无效等待
     * 
     * @param task 要执行的任务，不能为null
     * @param maxAttempts 最大重试次数，必须大于0
     * @param retryDelayMs 基础重试延迟时间（毫秒），必须大于0
     * @param <T> 任务返回值类型
     * @return 任务执行结果，包含成功/失败状态和执行时间
     * @throws IllegalArgumentException 当参数不合法时抛出
     * 
     * 使用示例：
     * <pre>
     * TaskResult&lt;String&gt; result = ConcurrentUtils.executeWithRetry(
     *     () -&gt; callRemoteService(),
     *     5,     // 最多重试5次
     *     1000   // 基础延迟1秒
     * );
     * if (result.isSuccess()) {
     *     String data = result.getData();
     *     // 处理成功结果
     * } else {
     *     Exception error = result.getException();
     *     // 处理失败情况
     * }
     * </pre>
     */
    public static <T> TaskResult<T> executeWithRetry(Callable<T> task, int maxAttempts, long retryDelayMs) {
        // 输入参数验证
        if (task == null) {
            throw new IllegalArgumentException("任务不能为null");
        }
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("最大重试次数必须大于0");
        }
        if (retryDelayMs <= 0) {
            throw new IllegalArgumentException("重试延迟时间必须大于0");
        }
        
        Exception lastException = null;
        long totalStartTime = System.nanoTime();
        
        logger.debug("开始执行带重试的任务，最大重试次数: {}, 基础延迟: {}ms", maxAttempts, retryDelayMs);
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            long attemptStartTime = System.nanoTime();
            
            try {
                // 执行任务
                T result = task.call();
                
                // 任务成功，计算总执行时间并返回
                long totalExecutionTime = (System.nanoTime() - totalStartTime) / 1_000_000; // 转换为毫秒
                
                if (attempt > 1) {
                    logger.info("任务在第{}次尝试后成功执行，总耗时: {}ms", attempt, totalExecutionTime);
                } else {
                    logger.debug("任务首次执行成功，耗时: {}ms", totalExecutionTime);
                }
                
                return TaskResult.success(result, totalExecutionTime);
                
            } catch (InterruptedException e) {
                // 线程中断，立即停止重试并恢复中断状态
                Thread.currentThread().interrupt();
                long totalExecutionTime = (System.nanoTime() - totalStartTime) / 1_000_000;
                logger.warn("任务执行被中断，已执行{}次尝试，总耗时: {}ms", attempt, totalExecutionTime);
                return TaskResult.failure(e, totalExecutionTime);
                
            } catch (Exception e) {
                lastException = e;
                long attemptExecutionTime = (System.nanoTime() - attemptStartTime) / 1_000_000;
                
                // 判断是否为最后一次尝试
                if (attempt == maxAttempts) {
                    long totalExecutionTime = (System.nanoTime() - totalStartTime) / 1_000_000;
                    logger.error("任务在第{}次尝试后仍然失败，总耗时: {}ms", attempt, totalExecutionTime, e);
                    return TaskResult.failure(lastException, totalExecutionTime);
                }
                
                // 计算下次重试的延迟时间（指数退避，但不超过30秒）
                long currentDelay = Math.min(retryDelayMs * (1L << (attempt - 1)), 30_000);
                
                logger.warn("任务第{}次尝试失败，耗时: {}ms，{}ms后进行第{}次重试。错误: {}", 
                        attempt, attemptExecutionTime, currentDelay, attempt + 1, e.getMessage());
                
                // 延迟后进行下次重试
                try {
                    Thread.sleep(currentDelay);
                } catch (InterruptedException ie) {
                    // 在延迟期间被中断，立即停止重试
                    Thread.currentThread().interrupt();
                    long totalExecutionTime = (System.nanoTime() - totalStartTime) / 1_000_000;
                    logger.warn("重试延迟期间被中断，停止重试，总耗时: {}ms", totalExecutionTime);
                    return TaskResult.failure(ie, totalExecutionTime);
                }
            }
        }
        
        // 理论上不会到达这里，但为了代码完整性保留
        long totalExecutionTime = (System.nanoTime() - totalStartTime) / 1_000_000;
        logger.error("任务执行完所有重试后失败，总耗时: {}ms", totalExecutionTime);
        return TaskResult.failure(lastException, totalExecutionTime);
    }
    
    /**
     * 使用默认参数的重试任务执行
     * 
     * 功能说明：
     * - 提供便捷的重试方法，使用预设的最佳实践参数
     * - 适用于大多数常见的重试场景
     * - 内部调用完整版本的executeWithRetry方法
     * 
     * 默认参数：
     * - 最大重试次数：3次（MAX_RETRY_ATTEMPTS）
     * - 基础延迟时间：1000毫秒（DEFAULT_RETRY_DELAY_MS）
     * 
     * @param task 要执行的任务，不能为null
     * @param <T> 任务返回值类型
     * @return 任务执行结果
     * @throws IllegalArgumentException 当任务为null时抛出
     * 
     * 使用示例：
     * <pre>
     * TaskResult&lt;String&gt; result = ConcurrentUtils.executeWithRetry(() -&gt; {
     *     return remoteApiCall();
     * });
     * </pre>
     */
    public static <T> TaskResult<T> executeWithRetry(Callable<T> task) {
        return executeWithRetry(task, MAX_RETRY_ATTEMPTS, DEFAULT_RETRY_DELAY_MS);
    }
    
    /**
     * 批量执行任务（使用默认批次大小）
     * 
     * 功能说明：
     * - 使用默认批次大小执行批量任务，简化API调用
     * - 适用于对批次大小没有特殊要求的场景
     * - 内部调用完整版本的executeBatch方法
     * 
     * @param tasks 要执行的任务列表，不能为null
     * @param timeout 每批次的超时时间，不能为null
     * @param <T> 任务返回值类型
     * @return 所有任务的执行结果列表
     * @throws IllegalArgumentException 当参数不合法时抛出
     */
    public static <T> List<TaskResult<T>> executeBatch(List<Callable<T>> tasks, Duration timeout) {
        return executeBatch(tasks, DEFAULT_BATCH_SIZE, timeout);
    }
    
    /**
     * 带超时控制的任务执行
     * 
     * 功能说明：
     * - 为长时间运行的任务提供超时保护，防止系统阻塞
     * - 基于CompletableFuture实现非阻塞超时控制
     * - 支持任务取消和资源清理
     * - 提供精确的执行时间统计
     * 
     * 执行机制：
     * - 使用CompletableFuture.supplyAsync异步执行任务
     * - 通过CompletableFuture.get(timeout)实现超时控制
     * - 超时后会尝试取消正在执行的任务
     * - 正确处理各种异常情况（超时、中断、执行异常）
     * 
     * 注意事项：
     * - 任务代码应该正确响应线程中断信号
     * - 超时后任务可能仍在后台执行，直到能够响应中断
     * - 建议在任务内部定期检查Thread.currentThread().isInterrupted()
     * 
     * @param supplier 要执行的任务供应者，不能为null
     * @param timeout 超时时间，必须为正数
     * @param <T> 任务返回值类型
     * @return 任务执行结果，包含成功/失败状态和执行时间
     * @throws IllegalArgumentException 当参数不合法时抛出
     * 
     * 使用示例：
     * <pre>
     * TaskResult&lt;String&gt; result = ConcurrentUtils.executeWithTimeout(
     *     () -&gt; {
     *         // 长时间运行的计算任务
     *         return heavyComputation();
     *     },
     *     Duration.ofSeconds(30)  // 30秒超时
     * );
     * </pre>
     */
    public static <T> TaskResult<T> executeWithTimeout(Supplier<T> supplier, Duration timeout) {
        // 输入参数验证
        if (supplier == null) {
            throw new IllegalArgumentException("任务供应者不能为null");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("超时时间必须为正数");
        }
        
        long startTime = System.nanoTime();
        CompletableFuture<T> future = null;
        
        try {
            logger.debug("开始执行带超时控制的任务，超时时间: {}ms", timeout.toMillis());
            
            // 异步执行任务
            future = CompletableFuture.supplyAsync(supplier);
            
            // 等待任务完成或超时
            T result = future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            
            long executionTime = (System.nanoTime() - startTime) / 1_000_000; // 转换为毫秒
            logger.debug("任务成功完成，耗时: {}ms", executionTime);
            
            return TaskResult.success(result, executionTime);
            
        } catch (java.util.concurrent.TimeoutException e) {
            // 任务超时
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            
            // 尝试取消任务
            if (future != null) {
                boolean cancelled = future.cancel(true); // 允许中断正在执行的任务
                logger.warn("任务执行超时({}ms)，任务取消{}, 超时时间: {}ms", 
                        executionTime, cancelled ? "成功" : "失败", timeout.toMillis());
            } else {
                logger.warn("任务执行超时({}ms)，超时时间: {}ms", executionTime, timeout.toMillis());
            }
            
            return TaskResult.failure(
                new java.util.concurrent.TimeoutException("任务执行超时，限制时间: " + timeout.toMillis() + "ms"), 
                executionTime
            );
            
        } catch (InterruptedException e) {
            // 当前线程被中断
            Thread.currentThread().interrupt(); // 恢复中断状态
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            
            // 尝试取消任务
            if (future != null) {
                future.cancel(true);
            }
            
            logger.warn("任务执行被中断，耗时: {}ms", executionTime);
            return TaskResult.failure(e, executionTime);
            
        } catch (java.util.concurrent.ExecutionException e) {
            // 任务执行过程中抛出异常
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            Throwable cause = e.getCause();
            Exception actualException = (cause instanceof Exception) ? (Exception) cause : e;
            
            logger.error("任务执行过程中发生异常，耗时: {}ms", executionTime, actualException);
            return TaskResult.failure(actualException, executionTime);
            
        } catch (Exception e) {
            // 其他未预期的异常
            long executionTime = (System.nanoTime() - startTime) / 1_000_000;
            logger.error("任务执行过程中发生未预期异常，耗时: {}ms", executionTime, e);
            return TaskResult.failure(e, executionTime);
        }
    }
    
    /**
     * 延迟执行工具方法
     * 
     * 功能说明：
     * - 提供线程安全的延迟执行功能
     * - 正确处理线程中断信号，支持优雅关闭
     * - 适用于需要暂停执行的业务场景
     * 
     * 实现细节：
     * - 使用Thread.sleep实现延迟
     * - 正确处理InterruptedException
     * - 在中断时恢复线程的中断状态
     * - 提供详细的日志记录
     * 
     * @param duration 延迟时间，不能为null且必须为非负数
     * @throws IllegalArgumentException 当延迟时间为null或负数时抛出
     * 
     * 使用示例：
     * <pre>
     * // 延迟5秒
     * ConcurrentUtils.delay(Duration.ofSeconds(5));
     * 
     * // 延迟500毫秒
     * ConcurrentUtils.delay(Duration.ofMillis(500));
     * </pre>
     */
    public static void delay(Duration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("延迟时间不能为null");
        }
        if (duration.isNegative()) {
            throw new IllegalArgumentException("延迟时间不能为负数");
        }
        if (duration.isZero()) {
            // 零延迟直接返回，不需要实际等待
            return;
        }
        
        long delayMs = duration.toMillis();
        logger.debug("开始延迟执行，延迟时间: {}ms", delayMs);
        
        try {
            Thread.sleep(delayMs);
            logger.debug("延迟执行完成");
        } catch (InterruptedException e) {
            // 恢复线程的中断状态
            Thread.currentThread().interrupt();
            logger.warn("延迟执行被中断，已执行时间: 未知", e);
        }
    }
    
    /**
     * 安全延迟执行（忽略中断）
     * 
     * 功能说明：
     * - 提供不响应中断的延迟执行功能
     * - 适用于必须完成延迟的场景（如防抖、限流等）
     * - 即使被中断也会继续等待剩余时间
     * 
     * 注意事项：
     * - 此方法不会响应线程中断，请谨慎使用
     * - 仅在明确需要忽略中断的场景下使用
     * - 不会恢复线程的中断状态
     * 
     * @param duration 延迟时间，不能为null且必须为非负数
     * @throws IllegalArgumentException 当延迟时间为null或负数时抛出
     */
    public static void delayUninterruptibly(Duration duration) {
        if (duration == null) {
            throw new IllegalArgumentException("延迟时间不能为null");
        }
        if (duration.isNegative()) {
            throw new IllegalArgumentException("延迟时间不能为负数");
        }
        if (duration.isZero()) {
            return;
        }
        
        long totalDelayMs = duration.toMillis();
        long startTime = System.nanoTime();
        long remainingMs = totalDelayMs;
        
        logger.debug("开始不可中断延迟执行，延迟时间: {}ms", totalDelayMs);
        
        while (remainingMs > 0) {
            try {
                Thread.sleep(remainingMs);
                break; // 正常完成延迟
            } catch (InterruptedException e) {
                // 计算剩余需要延迟的时间
                long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
                remainingMs = totalDelayMs - elapsedMs;
                
                if (remainingMs > 0) {
                    logger.debug("延迟执行被中断，继续等待剩余时间: {}ms", remainingMs);
                }
                // 不恢复中断状态，继续循环等待
            }
        }
        
        logger.debug("不可中断延迟执行完成");
    }
}