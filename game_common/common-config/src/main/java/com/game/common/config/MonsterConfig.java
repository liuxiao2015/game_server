package com.game.common.config;

import java.util.List;
import java.util.Map;

/**
 * 怪物配置
 *
 * @author lx
 * @date 2025/06/08
 */
@ConfigTable("monster.json")
public class MonsterConfig extends TableConfig {
    private int id;
    private String name;
    private int level;
    private Map<String, Integer> attributes; // 属性：HP、攻击、防御等
    private List<String> skills;            // 技能列表
    private List<DropItem> drops;           // 掉落列表
    
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
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public Map<String, Integer> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, Integer> attributes) {
        this.attributes = attributes;
    }
    
    public List<String> getSkills() {
        return skills;
    }
    
    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
    
    public List<DropItem> getDrops() {
        return drops;
    }
    
    public void setDrops(List<DropItem> drops) {
        this.drops = drops;
    }
    
    /**
     * 掉落物品配置
     */
    public static class DropItem {
        private int itemId;
        private float rate;    // 掉落率 (0.0-1.0)
        private int minCount;  // 最小数量
        private int maxCount;  // 最大数量
        
        public DropItem() {}
        
        public DropItem(int itemId, float rate, int minCount, int maxCount) {
            this.itemId = itemId;
            this.rate = rate;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
        
        public int getItemId() {
            return itemId;
        }
        
        public void setItemId(int itemId) {
            this.itemId = itemId;
        }
        
        public float getRate() {
            return rate;
        }
        
        public void setRate(float rate) {
            this.rate = rate;
        }
        
        public int getMinCount() {
            return minCount;
        }
        
        public void setMinCount(int minCount) {
            this.minCount = minCount;
        }
        
        public int getMaxCount() {
            return maxCount;
        }
        
        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }
    }
}