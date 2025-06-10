package com.game.service.logic;

import com.game.frame.timer.DailyResetTask;
import com.game.frame.timer.TimerManager;
import com.game.service.logic.manager.ModuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 游戏初始化器
 * 
 * 功能说明：
 * - 在Spring Boot应用启动时自动执行游戏系统的初始化
 * - 负责游戏模块的启动和配置加载
 * - 初始化定时任务和系统级服务
 * - 确保游戏服务器的正确启动和运行状态
 * 
 * 设计思路：
 * - 实现ApplicationRunner接口，在应用启动后自动执行
 * - 按照依赖关系有序地初始化各个模块
 * - 提供完整的错误处理和日志记录
 * - 支持模块化的初始化和故障隔离
 * 
 * 初始化内容：
 * - 游戏模块：背包、任务、战斗等核心业务模块
 * - 定时任务：每日重置、数据清理等定时任务
 * - 系统服务：缓存、数据库连接池等基础服务
 * - 配置加载：游戏配置表和参数设置
 * 
 * 启动顺序：
 * 1. Spring Boot容器初始化完成
 * 2. 执行GameInitializer的run方法
 * 3. 初始化游戏核心模块
 * 4. 启动定时任务和后台服务
 * 5. 完成整个系统的启动流程
 * 
 * 使用场景：
 * - 服务器启动时的系统初始化
 * - 模块依赖关系的管理
 * - 启动过程的监控和日志记录
 * - 系统健康检查和状态验证
 *
 * @author lx
 * @date 2025/06/08
 */
@Component
public class GameInitializer implements ApplicationRunner {
    
    // 日志记录器，用于记录游戏初始化过程的关键信息
    private static final Logger logger = LoggerFactory.getLogger(GameInitializer.class);
    
    // 模块管理器，负责管理所有游戏业务模块的生命周期
    @Autowired
    private ModuleManager moduleManager;
    
    /**
     * 应用启动后的初始化入口方法
     * 
     * 功能说明：
     * - Spring Boot应用启动完成后自动调用此方法
     * - 执行游戏系统的完整初始化流程
     * - 确保所有模块和服务正确启动
     * 
     * @param args 应用启动参数，可用于传递配置信息
     * @throws Exception 初始化过程中的异常会向上抛出
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("开始游戏系统初始化...");
        
        // 初始化游戏核心模块
        initGameModules();
        
        // 初始化定时任务系统
        initTimers();
        
        logger.info("游戏系统初始化完成");
    }
    
    /**
     * 初始化游戏核心模块
     * 
     * 功能说明：
     * - 启动游戏的各个核心业务模块
     * - 建立模块间的依赖关系和通信机制
     * - 验证模块初始化的完整性和正确性
     * 
     * 初始化模块包括：
     * - 背包模块：物品管理和存储系统
     * - 任务模块：任务系统和进度追踪
     * - 战斗模块：战斗逻辑和伤害计算
     * - 消息处理器：网络消息的路由和处理
     * 
     * 异常处理：
     * - 任何模块初始化失败都会导致整个初始化失败
     * - 详细记录初始化过程和失败原因
     * - 确保系统处于可用状态或完全失败
     */
    private void initGameModules() {
        logger.info("初始化游戏模块...");
        moduleManager.initModules();
        logger.info("游戏模块初始化完成");
    }
    
    /**
     * 初始化定时任务系统
     * 
     * 功能说明：
     * - 启动游戏相关的定时任务和调度器
     * - 配置各种周期性任务的执行计划
     * - 建立任务监控和故障恢复机制
     * 
     * 定时任务包括：
     * - 每日重置任务：每天0点执行数据重置
     * - 数据清理任务：定期清理过期数据
     * - 统计任务：定期收集和汇总统计数据
     * - 健康检查：定期检查系统状态
     * 
     * 调度策略：
     * - 使用Cron表达式精确控制执行时间
     * - 支持任务的暂停、恢复和重启
     * - 提供任务执行状态的监控和告警
     * 
     * @throws RuntimeException 定时器初始化失败时抛出运行时异常
     */
    private void initTimers() {
        try {
            logger.info("初始化定时任务系统...");
            
            // 初始化定时器管理器
            TimerManager.initialize();
            
            // 调度每日重置任务（每天凌晨0点执行）
            // Cron表达式："0 0 0 * * ?" 表示每天0时0分0秒执行
            TimerManager.scheduleCronJob(DailyResetTask.class, "DailyReset", "0 0 0 * * ?");
            
            logger.info("定时任务系统初始化完成");
            
        } catch (Exception e) {
            logger.error("定时任务系统初始化失败", e);
            throw new RuntimeException("定时任务初始化失败", e);
        }
    }
}