package com.game.frame.timer;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 定时器管理器
 * 支持Cron表达式、延迟任务、周期任务
 *
 * @author lx
 * @date 2025/06/08
 */
public class TimerManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TimerManager.class);
    private static Scheduler scheduler;
    
    /**
     * 初始化定时器管理器
     */
    public static void initialize() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        logger.info("Timer manager initialized");
    }
    
    /**
     * 关闭定时器管理器
     */
    public static void shutdown() throws SchedulerException {
        if (scheduler != null) {
            scheduler.shutdown();
            logger.info("Timer manager shutdown");
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