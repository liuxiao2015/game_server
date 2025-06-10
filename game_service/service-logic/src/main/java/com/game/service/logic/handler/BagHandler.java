package com.game.service.logic.handler;

import com.game.common.game.Result;
import com.game.service.logic.manager.ModuleManager;
import com.game.service.logic.module.bag.BagModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 背包消息处理器
 * 处理背包相关的协议消息
 *
 * @author lx
 * @date 2025/06/08
 */
public class BagHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(BagHandler.class);
    private final ModuleManager moduleManager;
    
    public BagHandler(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
    
    /**
     * 处理获取背包信息请求
     */
    public Result<BagModule.BagInfo> handleGetBagInfo(long playerId) {
        try {
            logger.debug("Handle get bag info: playerId={}", playerId);
            return moduleManager.getBagService().getBagInfo(playerId);
        } catch (Exception e) {
            logger.error("Failed to handle get bag info: playerId={}", playerId, e);
            return Result.failure("Failed to get bag info: " + e.getMessage());
        }
    }
    
    /**
     * 处理使用物品请求
     */
    public Result<Void> handleUseItem(long playerId, long itemUid, int count) {
        try {
            logger.debug("Handle use item: playerId={}, itemUid={}, count={}", playerId, itemUid, count);
            return moduleManager.getBagService().useItem(playerId, itemUid, count);
        } catch (Exception e) {
            logger.error("Failed to handle use item: playerId={}, itemUid={}, count={}", 
                    playerId, itemUid, count, e);
            return Result.failure("Failed to use item: " + e.getMessage());
        }
    }
    
    /**
     * 处理整理背包请求
     */
    public Result<Void> handleSortBag(long playerId) {
        try {
            logger.debug("Handle sort bag: playerId={}", playerId);
            return moduleManager.getBagService().sortBag(playerId);
        } catch (Exception e) {
            logger.error("Failed to handle sort bag: playerId={}", playerId, e);
            return Result.failure("Failed to sort bag: " + e.getMessage());
        }
    }
}