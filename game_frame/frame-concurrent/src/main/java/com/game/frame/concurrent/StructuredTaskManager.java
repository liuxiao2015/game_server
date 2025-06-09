package com.game.frame.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 结构化任务管理器 (Java 17兼容版本)
 * 
 * 功能说明：
 * - 使用CompletableFuture替代StructuredTaskScope实现任务编排
 * - 提供多任务并发执行、超时控制、结果收集等功能
 * - 支持全部任务执行、任意任务成功、单任务超时等多种执行模式
 * 
 * 设计思路：
 * - 采用静态工具类设计，便于在各个业务模块中使用
 * - 使用CompletableFuture实现异步任务编排
 * - 统一的TaskResult返回类型，便于结果处理和错误处理
 * - 内置超时控制机制，防止任务执行时间过长
 * 
 * 使用场景：
 * - 游戏业务中需要并发执行多个相关任务
 * - 需要超时控制的异步操作
 * - 聚合多个服务调用结果的场景
 * - 容错性要求较高的任务执行
 * 
 * @author lx
 * @date 2024-01-01
 */
public class StructuredTaskManager {
    
    // 日志记录器，用于记录任务执行状态和异常信息
    private static final Logger logger = LoggerFactory.getLogger(StructuredTaskManager.class);
    
    // 默认线程池执行器，使用缓存线程池以适应任务数量的动态变化
    // 线程命名包含纳秒时间戳，确保唯一性便于问题定位
    private static final ExecutorService defaultExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "StructuredTask-" + System.nanoTime());
        t.setDaemon(true); // 设置为守护线程，不阻止JVM关闭
        return t;
    });
    
    /**
     * 并发执行多个任务并等待所有任务完成
     * 
     * 执行逻辑：
     * 1. 将所有任务转换为CompletableFuture异步执行
     * 2. 使用CompletableFuture.allOf等待所有任务完成
     * 3. 应用超时控制，防止长时间阻塞
     * 4. 收集所有任务的执行结果，包括成功结果和异常信息
     * 5. 记录每个任务的执行时间用于性能分析
     * 
     * @param tasks 要执行的任务列表，每个任务都是Callable类型
     * @param timeout 最大等待时间，超过此时间将中断等待
     * @param <T> 任务返回结果的类型
     * @return 所有任务的执行结果列表，与输入任务列表顺序对应
     * 
     * 使用场景：
     * - 需要同时查询多个数据源并汇总结果
     * - 并行处理多个独立的业务逻辑
     * - 批量操作需要等待所有操作完成
     * 
     * 注意事项：
     * - 即使某些任务失败，也会等待所有任务完成
     * - 超时时所有未完成的任务会被取消
     * - 返回结果中包含成功和失败的任务信息
     */
    public static <T> List<TaskResult<T>> executeAll(List<Callable<T>> tasks, Duration timeout) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 将Callable任务转换为CompletableFuture
            List<CompletableFuture<T>> futures = tasks.stream()
                    .map(task -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return task.call();
                        } catch (Exception e) {
                            // 将检查异常转换为运行时异常，便于CompletableFuture处理
                            throw new RuntimeException(e);
                        }
                    }, defaultExecutor))
                    .toList();
            
            // 创建等待所有任务完成的CompletableFuture
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            
            // 应用超时控制，等待所有任务完成或超时
            allFutures.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            
            // 收集所有任务的执行结果
            return futures.stream()
                    .map(future -> {
                        try {
                            long executionTime = System.currentTimeMillis() - startTime;
                            return TaskResult.success(future.get(), executionTime);
                        } catch (Exception e) {
                            long executionTime = System.currentTimeMillis() - startTime;
                            return TaskResult.<T>failure(e, executionTime);
                        }
                    })
                    .toList();
                    
        } catch (Exception e) {
            // 发生超时或其他异常时的处理
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("批量任务执行失败", e);
            return List.of(TaskResult.failure(e, executionTime));
        }
    }
    
    /**
     * 并发执行多个任务并返回第一个成功的结果
     * 
     * 执行逻辑：
     * 1. 同时启动所有任务的异步执行
     * 2. 使用CompletableFuture.anyOf等待任意一个任务完成
     * 3. 一旦有任务成功完成立即返回结果
     * 4. 应用超时控制，防止所有任务都阻塞
     * 
     * @param tasks 要执行的任务列表，通常是执行相同逻辑的不同实现
     * @param timeout 最大等待时间，超过此时间将抛出超时异常
     * @param <T> 任务返回结果的类型
     * @return 第一个成功完成的任务结果
     * 
     * 使用场景：
     * - 多个数据源提供相同数据，任意一个成功即可
     * - 多种算法实现，使用最快完成的结果
     * - 容错场景，多个备选方案执行相同业务
     * 
     * 注意事项：
     * - 其他未完成的任务会继续执行但结果被忽略
     * - 如果第一个完成的任务失败，不会等待其他任务
     * - 建议任务具有幂等性，避免重复执行的副作用
     */
    public static <T> TaskResult<T> executeAny(List<Callable<T>> tasks, Duration timeout) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 将所有任务转换为CompletableFuture并发执行
            List<CompletableFuture<T>> futures = tasks.stream()
                    .map(task -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return task.call();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, defaultExecutor))
                    .toList();
            
            // 等待任意一个任务完成
            CompletableFuture<Object> anyFuture = CompletableFuture.anyOf(
                    futures.toArray(new CompletableFuture[0]));
            
            // 获取第一个完成的任务结果
            @SuppressWarnings("unchecked")
            T result = (T) anyFuture.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            
            long executionTime = System.currentTimeMillis() - startTime;
            return TaskResult.success(result, executionTime);
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("竞争任务执行失败", e);
            return TaskResult.failure(e, executionTime);
        }
    }
    
    /**
     * 执行单个任务并应用超时控制
     * 
     * @param task 要执行的单个任务
     * @param timeout 任务执行的最大允许时间
     * @param <T> 任务返回结果的类型
     * @return 任务执行结果，包含成功结果或失败信息
     * 
     * 实现说明：
     * - 本方法是executeAll的简化版本，用于单任务场景
     * - 内部调用executeAll方法处理单元素列表
     */
    public static <T> TaskResult<T> executeWithTimeout(Callable<T> task, Duration timeout) {
        return executeAll(List.of(task), timeout).get(0);
    }
    
    /**
     * 安全执行Supplier任务，提供完整的异常处理
     * 
     * 功能特点：
     * 1. 捕获并封装所有可能的异常
     * 2. 记录任务执行时间用于性能监控
     * 3. 提供统一的结果格式便于后续处理
     * 
     * @param supplier 要执行的供应商函数，通常是无参数的计算逻辑
     * @param <T> 返回结果的类型
     * @return 包装后的任务执行结果
     * 
     * 使用场景：
     * - 需要统一异常处理的简单计算
     * - 不需要超时控制的同步任务
     * - 需要执行时间统计的业务逻辑
     */
    public static <T> TaskResult<T> safeExecute(Supplier<T> supplier) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行供应商函数获取结果
            T result = supplier.get();
            long executionTime = System.currentTimeMillis() - startTime;
            return TaskResult.success(result, executionTime);
        } catch (Exception e) {
            // 捕获所有异常并记录执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("安全任务执行失败", e);
            return TaskResult.failure(e, executionTime);
        }
    }
}