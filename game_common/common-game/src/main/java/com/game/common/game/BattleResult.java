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
    
    public BattleResult(boolean victory, int expGain, List<Item> drops) {
        this.victory = victory;
        this.expGain = expGain;
        this.drops = drops;
    }
    
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