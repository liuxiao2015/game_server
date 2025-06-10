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
 * 在应用启动时初始化游戏模块和定时任务
 *
 * @author lx
 * @date 2025/06/08
 */
@Component
public class GameInitializer implements ApplicationRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(GameInitializer.class);
    
    @Autowired
    private ModuleManager moduleManager;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Starting game initialization...");
        
        // 初始化游戏模块
        initGameModules();
        
        // 初始化定时器
        initTimers();
        
        logger.info("Game initialization completed successfully");
    }
    
    /**
     * 初始化游戏模块
     */
    private void initGameModules() {
        logger.info("Initializing game modules...");
        moduleManager.initModules();
        logger.info("Game modules initialized");
    }
    
    /**
     * 初始化定时器
     */
    private void initTimers() {
        try {
            logger.info("Initializing timers...");
            
            // 初始化定时器管理器
            TimerManager.initialize();
            
            // 调度每日重置任务（每天0点执行）
            TimerManager.scheduleCronJob(DailyResetTask.class, "DailyReset", "0 0 0 * * ?");
            
            logger.info("Timers initialized");
            
        } catch (Exception e) {
            logger.error("Failed to initialize timers", e);
            throw new RuntimeException("Timer initialization failed", e);
        }
    }
}