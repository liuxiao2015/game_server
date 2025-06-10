package com.game.service.logic.handler;

import com.game.common.game.BattleResult;
import com.game.common.game.Result;
import com.game.service.logic.manager.ModuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 战斗消息处理器
 * 处理战斗相关的协议消息
 *
 * @author lx
 * @date 2025/06/08
 */
public class BattleHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(BattleHandler.class);
    private final ModuleManager moduleManager;
    
    public BattleHandler(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
    
    /**
     * 处理战斗开始请求
     */
    public Result<BattleResult> handleBattleStart(long playerId, int monsterId) {
        try {
            logger.debug("Handle battle start: playerId={}, monsterId={}", playerId, monsterId);
            return moduleManager.getBattleModule().startBattle(playerId, monsterId);
        } catch (Exception e) {
            logger.error("Failed to handle battle start: playerId={}, monsterId={}", 
                    playerId, monsterId, e);
            return Result.failure("Failed to start battle: " + e.getMessage());
        }
    }
}