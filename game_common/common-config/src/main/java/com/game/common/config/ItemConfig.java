package com.game.common.config;

import java.util.Map;

/**
 * 物品配置
 *
 * @author lx
 * @date 2025/06/08
 */
@ConfigTable("item.json")
public class ItemConfig extends TableConfig {
    private int id;
    private String name;
    private int type;
    private int quality;
    private String icon;
    private Map<String, Integer> attributes;
    private boolean stackable;
    private int maxStack;
    
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
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public int getQuality() {
        return quality;
    }
    
    public void setQuality(int quality) {
        this.quality = quality;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public Map<String, Integer> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Map<String, Integer> attributes) {
        this.attributes = attributes;
    }
    
    /**

    
     * isStackable方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
    public boolean isStackable() {
        return stackable;
    }
    
    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }
    
    public int getMaxStack() {
        return maxStack;
    }
    
    public void setMaxStack(int maxStack) {
        this.maxStack = maxStack;
    }
}