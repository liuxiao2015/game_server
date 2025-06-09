package com.game.common.config;

import java.util.List;
import java.util.Map;

/**
 * 任务配置
 *
 * @author lx
 * @date 2025/06/08
 */
@ConfigTable("task.json")
public class TaskConfig extends TableConfig {
    private int id;
    private String name;
    private String description;
    private int type;
    private Map<String, Integer> requirements; // 任务需求
    private List<ItemReward> rewards;          // 任务奖励
    private int expReward;                     // 经验奖励
    
    @Override
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public Map<String, Integer> getRequirements() {
        return requirements;
    }
    
    public void setRequirements(Map<String, Integer> requirements) {
        this.requirements = requirements;
    }
    
    public List<ItemReward> getRewards() {
        return rewards;
    }
    
    public void setRewards(List<ItemReward> rewards) {
        this.rewards = rewards;
    }
    
    public int getExpReward() {
        return expReward;
    }
    
    public void setExpReward(int expReward) {
        this.expReward = expReward;
    }
    
    /**
     * 物品奖励配置
     */
    public static class ItemReward {
        private int itemId;
        private int count;
        
        public ItemReward() {}
        
        public ItemReward(int itemId, int count) {
            this.itemId = itemId;
            this.count = count;
        }
        
        public int getItemId() {
            return itemId;
        }
        
        public void setItemId(int itemId) {
            this.itemId = itemId;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
}