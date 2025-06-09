package com.game.service.logic.manager;

import com.game.service.logic.module.bag.BagModule;
import com.game.service.logic.module.bag.BagService;
import com.game.service.logic.module.battle.BattleModule;
import com.game.service.logic.module.task.TaskModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 模块管理器
 * 负责注册所有游戏模块、初始化模块、玩家登录初始化
 *
 * @author lx
 * @date 2025/06/08
 */
@Component
public class ModuleManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);
    
    // 游戏模块
    private BagModule bagModule;
    private BagService bagService;
    private TaskModule taskModule;
    private BattleModule battleModule;
    
    /**
     * 初始化所有模块
     */
    public void initModules() {
        logger.info("Initializing game modules...");
        
        // 初始化背包模块
        bagModule = new BagModule();
        bagService = new BagService(bagModule);
        
        // 初始化任务模块
        taskModule = new TaskModule();
        
        // 初始化战斗模块
        battleModule = new BattleModule();
        
        logger.info("All game modules initialized successfully");
    }
    
    /**
     * 玩家登录时初始化
     */
    public void onPlayerLogin(long playerId) {
        logger.info("Initializing modules for player: {}", playerId);
        
        // 初始化玩家背包
        bagModule.initPlayerBag(playerId);
        
        // 初始化玩家任务
        taskModule.initPlayerTasks(playerId);
        
        logger.info("Player modules initialized: playerId={}", playerId);
    }
    
    /**
     * 玩家登出时清理
     */
    public void onPlayerLogout(long playerId) {
        logger.info("Player logout cleanup: playerId={}", playerId);
        // TODO: 实现玩家数据持久化等清理工作
    }
    
    // Getters for modules
    public BagModule getBagModule() {
        return bagModule;
    }
    
    public BagService getBagService() {
        return bagService;
    }
    
    public TaskModule getTaskModule() {
        return taskModule;
    }
    
    public BattleModule getBattleModule() {
        return battleModule;
    }
}