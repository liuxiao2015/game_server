package com.game.service.logic.module.bag;

import com.game.common.game.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 背包服务
 * 提供背包相关的业务操作
 *
 * @author lx
 * @date 2025/06/08
 */
public class BagService {
    
    private static final Logger logger = LoggerFactory.getLogger(BagService.class);
    private final BagModule bagModule;
    
    public BagService(BagModule bagModule) {
        this.bagModule = bagModule;
    }
    
    /**
     * 添加物品
     */
    public Result<Void> addItem(long playerId, int itemId, int count) {
        try {
            return bagModule.addItem(playerId, itemId, count);
        } catch (Exception e) {
            logger.error("Failed to add item: playerId={}, itemId={}, count={}", playerId, itemId, count, e);
            return Result.failure("Failed to add item: " + e.getMessage());
        }
    }
    
    /**
     * 使用物品
     */
    public Result<Void> useItem(long playerId, long itemUid, int count) {
        try {
            return bagModule.useItem(playerId, itemUid, count);
        } catch (Exception e) {
            logger.error("Failed to use item: playerId={}, itemUid={}, count={}", playerId, itemUid, count, e);
            return Result.failure("Failed to use item: " + e.getMessage());
        }
    }
    
    /**
     * 获取背包信息
     */
    public Result<BagModule.BagInfo> getBagInfo(long playerId) {
        try {
            return bagModule.getBagInfo(playerId);
        } catch (Exception e) {
            logger.error("Failed to get bag info: playerId={}", playerId, e);
            return Result.failure("Failed to get bag info: " + e.getMessage());
        }
    }
    
    /**
     * 整理背包
     */
    public Result<Void> sortBag(long playerId) {
        try {
            // TODO: 实现背包整理逻辑
            logger.debug("Sort bag: playerId={}", playerId);
            return Result.success();
        } catch (Exception e) {
            logger.error("Failed to sort bag: playerId={}", playerId, e);
            return Result.failure("Failed to sort bag: " + e.getMessage());
        }
    }
}