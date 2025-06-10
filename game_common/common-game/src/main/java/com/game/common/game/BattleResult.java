package com.game.common.game;

import java.util.List;

/**
 * 战斗结果
 *
 * @author lx
 * @date 2025/06/08
 */
public class BattleResult {
    
    private boolean victory;
    private int expGain;
    private List<Item> drops;
    
    public BattleResult() {}
    
    /**

    
     * BattleResult方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
    public BattleResult(boolean victory, int expGain, List<Item> drops) {
        this.victory = victory;
        this.expGain = expGain;
        this.drops = drops;
    }
    
    /**

    
     * isVictory方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
    public boolean isVictory() {
        return victory;
    }
    
    public void setVictory(boolean victory) {
        this.victory = victory;
    }
    
    public int getExpGain() {
        return expGain;
    }
    
    public void setExpGain(int expGain) {
        this.expGain = expGain;
    }
    
    public List<Item> getDrops() {
        return drops;
    }
    
    public void setDrops(List<Item> drops) {
        this.drops = drops;
    }
}