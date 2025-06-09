package com.game.service.logic.manager;

import com.game.service.logic.handler.BagHandler;
import com.game.service.logic.handler.BattleHandler;
import com.game.service.logic.handler.TaskHandler;
import com.game.service.logic.module.bag.BagModule;
import com.game.service.logic.module.bag.BagService;
import com.game.service.logic.module.battle.BattleModule;
import com.game.service.logic.module.task.TaskModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 游戏模块管理器
 * 
 * 功能说明：
 * - 统一管理所有游戏业务模块的生命周期
 * - 负责模块间的依赖注入和组装
 * - 提供玩家登录/登出时的模块初始化和清理
 * - 集中管理消息处理器的注册和路由
 * 
 * 设计思路：
 * - 采用中心化管理模式，避免模块间直接依赖
 * - 实现模块的懒加载和按需初始化
 * - 提供统一的模块访问接口
 * - 支持模块的热插拔和动态扩展
 * 
 * 管理的核心模块：
 * - 背包模块(BagModule)：物品管理、容量控制、过期清理
 * - 任务模块(TaskModule)：任务进度、奖励发放、完成验证
 * - 战斗模块(BattleModule)：战斗创建、伤害计算、结果处理
 * - 消息处理器：各模块的网络消息路由和处理
 * 
 * 生命周期管理：
 * 1. 系统启动：初始化所有模块和处理器
 * 2. 玩家登录：为玩家初始化个人数据
 * 3. 游戏运行：处理业务逻辑和消息路由
 * 4. 玩家登出：清理玩家相关的内存数据
 * 5. 系统关闭：释放所有模块资源
 * 
 * 使用场景：
 * - 服务器启动时的模块装配
 * - 玩家会话管理
 * - 模块间通信协调
 * - 系统资源管理
 *
 * @author lx
 * @date 2025/06/08
 */
@Component
public class ModuleManager {
    
    // 日志记录器，用于记录模块管理的关键操作和异常信息
    private static final Logger logger = LoggerFactory.getLogger(ModuleManager.class);
    
    // ========== 游戏核心业务模块 ==========
    // 背包模块：负责玩家物品管理、容量控制、物品使用等功能
    private BagModule bagModule;
    // 背包服务：封装背包业务逻辑，提供对外服务接口
    private BagService bagService;
    // 任务模块：负责任务系统、进度追踪、奖励发放等功能
    private TaskModule taskModule;
    // 战斗模块：负责战斗逻辑、伤害计算、技能系统等功能
    private BattleModule battleModule;
    
    // ========== 网络消息处理器 ==========
    // 背包消息处理器：处理客户端背包相关的网络请求
    private BagHandler bagHandler;
    // 任务消息处理器：处理客户端任务相关的网络请求
    private TaskHandler taskHandler;
    // 战斗消息处理器：处理客户端战斗相关的网络请求
    private BattleHandler battleHandler;
    
    /**
     * 初始化所有游戏模块
     * 
     * 初始化顺序和依赖关系：
     * 1. 首先初始化核心业务模块（无依赖关系）
     * 2. 然后初始化服务层（依赖业务模块）
     * 3. 最后初始化消息处理器（依赖模块管理器）
     * 
     * 初始化内容：
     * - 创建模块实例并进行基础配置
     * - 建立模块间的依赖关系
     * - 注册消息处理器和路由规则
     * - 加载配置数据和初始化缓存
     * 
     * 异常处理：
     * - 如果任何模块初始化失败，整个系统应停止启动
     * - 记录详细的错误信息便于问题定位
     * - 确保资源的正确释放，避免内存泄漏
     */
    public void initModules() {
        logger.info("开始初始化游戏模块...");
        
        try {
            // ========== 初始化核心业务模块 ==========
            // 背包模块：管理玩家物品、容量、过期等
            bagModule = new BagModule();
            logger.debug("背包模块初始化完成");
            
            // 背包服务：封装背包业务逻辑
            bagService = new BagService(bagModule);
            logger.debug("背包服务初始化完成");
            
            // 任务模块：管理玩家任务状态和进度
            taskModule = new TaskModule();
            logger.debug("任务模块初始化完成");
            
            // 战斗模块：处理战斗逻辑和计算
            battleModule = new BattleModule();
            logger.debug("战斗模块初始化完成");
            
            // ========== 初始化消息处理器 ==========
            // 各处理器需要依赖模块管理器来访问业务模块
            bagHandler = new BagHandler(this);
            logger.debug("背包消息处理器初始化完成");
            
            taskHandler = new TaskHandler(this);
            logger.debug("任务消息处理器初始化完成");
            
            battleHandler = new BattleHandler(this);
            logger.debug("战斗消息处理器初始化完成");
            
            logger.info("所有游戏模块初始化成功");
            
        } catch (Exception e) {
            logger.error("游戏模块初始化失败", e);
            throw new RuntimeException("模块初始化失败，系统无法启动", e);
        }
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
    
    // Getters for handlers
    public BagHandler getBagHandler() {
        return bagHandler;
    }
    
    public TaskHandler getTaskHandler() {
        return taskHandler;
    }
    
    public BattleHandler getBattleHandler() {
        return battleHandler;
    }
}