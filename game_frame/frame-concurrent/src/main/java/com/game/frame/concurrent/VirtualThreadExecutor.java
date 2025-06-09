package com.game.frame.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 虚拟线程执行器管理类 (Java 17兼容版本)
 * 
 * 功能说明：
 * - 管理线程生命周期，提供统计信息和优雅关闭机制
 * - 在Java 17环境下使用缓存线程池替代虚拟线程
 * - 提供任务提交、执行监控、资源管理等核心功能
 * 
 * 设计思路：
 * - 采用装饰器模式封装ExecutorService，添加统计和监控能力
 * - 使用原子类保证统计数据的线程安全性
 * - 通过自定义ThreadFactory实现线程命名和属性设置
 * 
 * 使用场景：
 * - 游戏服务器高并发任务处理
 * - 异步业务逻辑执行
 * - 需要监控和统计的线程池场景
 * 
 * @author lx
 * @date 2024-01-01
 */
public class VirtualThreadExecutor {
    
    // 日志记录器，用于输出执行状态和错误信息
    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadExecutor.class);
    
    // 底层线程池执行器，负责实际的任务执行
    private final ExecutorService executor;
    // 线程名称前缀，用于标识不同用途的线程池
    private final String namePrefix;
    // 已提交任务计数器，记录总的任务提交数量
    private final AtomicLong submittedTasks = new AtomicLong(0);
    // 已完成任务计数器，记录成功完成的任务数量
    private final AtomicLong completedTasks = new AtomicLong(0);
    // 被拒绝任务计数器，记录因各种原因被拒绝的任务数量
    private final AtomicLong rejectedTasks = new AtomicLong(0);
    // 线程计数器，用于为新创建的线程分配唯一编号
    private final AtomicLong threadCounter = new AtomicLong(0);
    
    // 关闭状态标志，使用volatile确保线程间可见性
    private volatile boolean shutdown = false;
    
    /**
     * 创建使用默认名称前缀的虚拟线程执行器
     * 
     * 使用说明：
     * - 默认线程名前缀为"GameThread"
     * - 适用于通用的游戏业务场景
     */
    public VirtualThreadExecutor() {
        this("GameThread");
    }
    
    /**
     * 创建使用自定义名称前缀的虚拟线程执行器
     * 
     * @param namePrefix 线程名称前缀，用于区分不同业务模块的线程
     *                   建议使用业务相关的有意义名称，如"LoginThread"、"BattleThread"等
     */
    public VirtualThreadExecutor(String namePrefix) {
        this.namePrefix = namePrefix;
        // 创建缓存线程池，使用自定义线程工厂
        this.executor = Executors.newCachedThreadPool(this::createThread);
        logger.info("虚拟线程执行器已创建，线程名前缀: {} (Java 17模式)", namePrefix);
    }
    
    /**
     * 提交任务进行异步执行
     * 
     * 业务逻辑：
     * 1. 检查执行器是否已关闭，如已关闭则拒绝任务并抛出异常
     * 2. 增加已提交任务计数
     * 3. 将任务包装后提交给线程池执行
     * 4. 在任务执行完成后更新完成计数，异常时记录错误日志
     * 
     * @param task 要执行的任务，不能为null
     * @throws IllegalStateException 当执行器已关闭时抛出此异常
     * 
     * 注意事项：
     * - 任务执行过程中的异常会被捕获并记录，不会向外传播
     * - 建议在任务中进行适当的异常处理
     */
    public void submit(Runnable task) {
        // 检查执行器状态，防止在关闭后继续接收任务
        if (shutdown) {
            rejectedTasks.incrementAndGet();
            throw new IllegalStateException("执行器已关闭，无法提交新任务");
        }
        
        // 增加已提交任务计数
        submittedTasks.incrementAndGet();
        // 提交任务到线程池，包装异常处理逻辑
        executor.submit(() -> {
            try {
                task.run();
                // 任务成功完成，增加完成计数
                completedTasks.incrementAndGet();
            } catch (Exception e) {
                // 记录任务执行异常，但不影响其他任务执行
                logger.error("任务执行失败", e);
            }
        });
    }
    
    /**
     * 创建新线程的工厂方法
     * 
     * 实现细节：
     * - 为每个线程设置有意义的名称，包含前缀和唯一编号
     * - 设置为守护线程，确保JVM能够正常退出
     * - 线程编号使用原子递增，保证唯一性
     * 
     * @param r 要在新线程中执行的任务
     * @return 配置好的新线程实例
     */
    private Thread createThread(Runnable r) {
        Thread thread = new Thread(r);
        // 设置线程名称：前缀-唯一编号
        thread.setName(namePrefix + "-" + threadCounter.incrementAndGet());
        // 设置为守护线程，避免阻止JVM关闭
        thread.setDaemon(true);
        return thread;
    }
    
    /**
     * 获取已提交任务总数
     * 
     * @return 自执行器创建以来提交的任务总数
     */
    public long getSubmittedTaskCount() {
        return submittedTasks.get();
    }
    
    /**
     * 获取已完成任务总数
     * 
     * @return 成功执行完成的任务总数（不包括执行失败的任务）
     */
    public long getCompletedTaskCount() {
        return completedTasks.get();
    }
    
    /**
     * 获取被拒绝任务总数
     * 
     * @return 因执行器关闭等原因被拒绝的任务总数
     */
    public long getRejectedTaskCount() {
        return rejectedTasks.get();
    }
    
    /**
     * 启动执行器的优雅关闭流程
     * 
     * 关闭流程：
     * 1. 设置关闭标志，拒绝新任务提交
     * 2. 调用底层线程池的shutdown方法
     * 3. 已提交的任务会继续执行完成
     * 4. 输出统计信息到日志
     * 
     * 注意事项：
     * - 此方法不会等待已提交任务完成
     * - 重复调用是安全的
     * - 建议在应用关闭时调用此方法
     */
    public void shutdown() {
        if (!shutdown) {
            shutdown = true;
            executor.shutdown();
            logger.info("虚拟线程执行器开始优雅关闭。统计信息 - 已提交: {}, 已完成: {}, 已拒绝: {}", 
                    submittedTasks.get(), completedTasks.get(), rejectedTasks.get());
        }
    }
    
    /**
     * 强制立即关闭执行器
     * 
     * 关闭流程：
     * 1. 设置关闭标志，拒绝新任务提交
     * 2. 尝试停止所有正在执行的任务
     * 3. 输出警告级别的统计信息
     * 
     * 注意事项：
     * - 正在执行的任务可能会被中断
     * - 应该优先使用shutdown()方法
     * - 仅在紧急情况下使用此方法
     */
    public void shutdownNow() {
        if (!shutdown) {
            shutdown = true;
            executor.shutdownNow();
            logger.warn("虚拟线程执行器被强制关闭。统计信息 - 已提交: {}, 已完成: {}, 已拒绝: {}", 
                    submittedTasks.get(), completedTasks.get(), rejectedTasks.get());
        }
    }
    
    /**
     * 检查执行器是否已关闭
     * 
     * @return true表示执行器已关闭，false表示仍在运行
     */
    public boolean isShutdown() {
        return shutdown;
    }
}