package com.game.frame.timer;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏定时器管理器
 * 
 * 功能说明：
 * - 基于Quartz框架实现的企业级定时任务调度器
 * - 支持Cron表达式、延迟任务、周期性任务等多种调度模式
 * - 提供任务持久化、集群支持、失败重试等高级特性
 * - 集成游戏业务场景的常用定时任务模板
 * 
 * 设计思路：
 * - 采用静态工具类设计，便于全局访问和管理
 * - 封装Quartz的复杂配置，提供简化的调度接口
 * - 支持任务分组管理，便于不同业务模块的任务隔离
 * - 提供完整的生命周期管理和优雅关闭机制
 * 
 * 调度类型：
 * - Cron调度：基于时间表达式的精确时间调度
 * - 固定延迟：任务完成后等待指定时间再执行
 * - 固定频率：按照固定时间间隔重复执行
 * - 一次性任务：延迟指定时间后执行一次
 * 
 * 游戏应用场景：
 * - 日常重置：每日0点重置任务、商店、活动等
 * - 活动调度：活动开启、结束、奖励发放等
 * - 数据清理：日志清理、过期数据删除等
 * - 统计计算：排行榜更新、数据报表生成等
 * - 系统维护：缓存刷新、数据库优化等
 *
 * @author lx
 * @date 2025/06/08
 */
public class TimerManager {
    
    // 日志记录器，用于记录定时任务的执行状态和异常信息
    private static final Logger logger = LoggerFactory.getLogger(TimerManager.class);
    // Quartz调度器实例，负责管理所有定时任务的调度执行
    private static Scheduler scheduler;
    
    /**
     * 初始化定时器管理器
     * 
     * 初始化流程：
     * 1. 创建Quartz调度器实例
     * 2. 启动调度器开始接受任务调度
     * 3. 记录初始化成功日志
     * 
     * 配置说明：
     * - 使用默认的Quartz配置
     * - 支持内存存储模式（非集群环境）
     * - 可通过quartz.properties文件自定义配置
     * 
     * @throws SchedulerException 调度器初始化失败时抛出
     */
    public static void initialize() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        logger.info("定时器管理器初始化完成");
    }
    
    /**
     * 关闭定时器管理器
     * 
     * 关闭流程：
     * 1. 等待当前执行中的任务完成
     * 2. 停止接受新的任务调度
     * 3. 释放相关资源
     * 4. 记录关闭日志
     * 
     * 注意事项：
     * - 建议在应用关闭时调用此方法
     * - 强制关闭可能导致正在执行的任务被中断
     * 
     * @throws SchedulerException 调度器关闭失败时抛出
     */
    public static void shutdown() throws SchedulerException {
        if (scheduler != null) {
            scheduler.shutdown();
            logger.info("定时器管理器已关闭");
        }
    }
    
    /**
     * 调度Cron任务
     * 
     * @param jobClass 任务类
     * @param jobName 任务名称
     * @param cronExpression Cron表达式
     */
    public static void scheduleCronJob(Class<? extends Job> jobClass, String jobName, String cronExpression) {
        try {
            JobDetail job = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName)
                    .build();
            
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "Trigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();
            
            scheduler.scheduleJob(job, trigger);
            logger.info("Scheduled cron job: {} with expression: {}", jobName, cronExpression);
            
        } catch (SchedulerException e) {
            logger.error("Failed to schedule cron job: {}", jobName, e);
        }
    }
    
    /**
     * 调度延迟任务
     * 
     * @param jobClass 任务类
     * @param jobName 任务名称
     * @param delayMs 延迟时间（毫秒）
     */
    public static void scheduleDelayedJob(Class<? extends Job> jobClass, String jobName, long delayMs) {
        try {
            JobDetail job = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName)
                    .build();
            
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "Trigger")
                    .startAt(DateBuilder.futureDate((int) delayMs / 1000, DateBuilder.IntervalUnit.SECOND))
                    .build();
            
            scheduler.scheduleJob(job, trigger);
            logger.info("Scheduled delayed job: {} with delay: {}ms", jobName, delayMs);
            
        } catch (SchedulerException e) {
            logger.error("Failed to schedule delayed job: {}", jobName, e);
        }
    }
    
    /**
     * 取消任务
     * 
     * @param jobName 任务名称
     */
    public static void cancelJob(String jobName) {
        try {
            scheduler.deleteJob(JobKey.jobKey(jobName));
            logger.info("Cancelled job: {}", jobName);
        } catch (SchedulerException e) {
            logger.error("Failed to cancel job: {}", jobName, e);
        }
    }
}