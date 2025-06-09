package com.game.service.logic.module.battle;

import com.game.common.game.BattleResult;
import com.game.common.game.Item;
import com.game.common.game.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 战斗模块
 * 负责战斗创建、战斗流程控制、战斗结算
 *
 * @author lx
 * @date 2025/06/08
 */
public class BattleModule {
    
    private static final Logger logger = LoggerFactory.getLogger(BattleModule.class);
    private static final Random random = new Random();
    
    /**
     * 开始战斗
     */
    public Result<BattleResult> startBattle(long playerId, int monsterId) {
        try {
            logger.debug("Starting battle: playerId={}, monsterId={}", playerId, monsterId);
            
            // 简单的战斗逻辑模拟
            boolean victory = random.nextBoolean(); // 50%胜率
            int expGain = victory ? 100 : 50;
            List<Item> drops = new ArrayList<>();
            
            if (victory) {
                // 胜利时有掉落
                if (random.nextFloat() < 0.3f) { // 30%掉落率
                    Item drop = new Item(System.currentTimeMillis(), 1001, 1); // 掉落物品ID 1001
                    drops.add(drop);
                }
            }
            
            BattleResult result = new BattleResult(victory, expGain, drops);
            logger.debug("Battle result: playerId={}, victory={}, expGain={}, drops={}", 
                    playerId, victory, expGain, drops.size());
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("Failed to start battle: playerId={}, monsterId={}", playerId, monsterId, e);
            return Result.failure("Battle failed: " + e.getMessage());
        }
    }
    
    /**
     * 计算伤害
     */
    private int calculateDamage(int attack, int defense) {
        int baseDamage = Math.max(1, attack - defense);
        // 加入随机因子 ±20%
        float randomFactor = 0.8f + random.nextFloat() * 0.4f;
        return (int) (baseDamage * randomFactor);
    }
    
    /**
     * 判断暴击
     */
    private boolean isCritical(float critRate) {
        return random.nextFloat() < critRate;
    }
}