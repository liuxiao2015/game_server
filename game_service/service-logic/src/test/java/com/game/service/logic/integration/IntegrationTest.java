package com.game.service.logic.integration;

import com.game.service.logic.manager.ModuleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 集成测试
 * 测试各个模块的集成情况
 *
 * @author lx
 * @date 2025/06/08
 */
public class IntegrationTest {
    
    private ModuleManager moduleManager;
    private static final long TEST_PLAYER_ID = 12345L;
    
    @BeforeEach
    public void setUp() {
        moduleManager = new ModuleManager();
        moduleManager.initModules();
        moduleManager.onPlayerLogin(TEST_PLAYER_ID);
    }
    
    @Test
    public void testModuleInitialization() {
        // 测试模块是否正确初始化
        assertNotNull(moduleManager.getBagModule());
        assertNotNull(moduleManager.getBagService());
        assertNotNull(moduleManager.getTaskModule());
        assertNotNull(moduleManager.getBattleModule());
        
        // 测试处理器是否正确初始化
        assertNotNull(moduleManager.getBagHandler());
        assertNotNull(moduleManager.getTaskHandler());
        assertNotNull(moduleManager.getBattleHandler());
    }
    
    @Test
    public void testGameFlow() {
        // 测试完整的游戏流程
        
        // 1. 给玩家添加物品
        var addResult = moduleManager.getBagService().addItem(TEST_PLAYER_ID, 1001, 1);
        assertTrue(addResult.isSuccess());
        
        // 2. 查看背包
        var bagInfo = moduleManager.getBagService().getBagInfo(TEST_PLAYER_ID);
        assertTrue(bagInfo.isSuccess());
        assertEquals(1, bagInfo.getData().getItems().size());
        
        // 3. 接取任务
        var acceptTask = moduleManager.getTaskModule().acceptTask(TEST_PLAYER_ID, 2001);
        assertTrue(acceptTask.isSuccess());
        
        // 4. 查看任务列表
        var taskList = moduleManager.getTaskModule().getTaskList(TEST_PLAYER_ID);
        assertTrue(taskList.isSuccess());
        assertEquals(1, taskList.getData().size());
        
        // 5. 开始战斗
        var battleResult = moduleManager.getBattleModule().startBattle(TEST_PLAYER_ID, 1001);
        assertTrue(battleResult.isSuccess());
        assertNotNull(battleResult.getData());
    }
}