package com.game.common.config;

import java.util.List;
import java.util.Map;

/**
 * 游戏任务配置类
 * 
 * 功能说明：
 * - 定义游戏任务系统的配置数据结构
 * - 包含任务的基本信息、完成条件和奖励设置
 * - 支持多种任务类型和复杂的任务需求配置
 * - 提供任务奖励的物品和经验配置
 * 
 * 设计思路：
 * - 继承TableConfig实现统一的配置管理机制
 * - 使用@ConfigTable注解关联task.json配置文件
 * - 支持灵活的任务需求配置（键值对映射）
 * - 提供结构化的物品奖励配置列表
 * 
 * 配置结构：
 * - 基本信息：任务ID、名称、描述、类型
 * - 任务需求：完成条件的键值对映射
 * - 奖励设置：物品奖励列表和经验奖励
 * 
 * 任务类型：
 * - 根据type字段区分不同类型的任务
 * - 支持主线任务、支线任务、日常任务等
 * - 不同类型任务有不同的完成逻辑和奖励机制
 * 
 * 需求配置：
 * - 使用Map结构支持多种任务需求类型
 * - 例如：{"kill_monster_1001": 10, "collect_item_2001": 5}
 * - 键为需求类型，值为需求数量
 * 
 * 奖励配置：
 * - 物品奖励：支持多种物品的组合奖励
 * - 经验奖励：任务完成后获得的经验值
 * - 支持随机奖励和固定奖励的配置
 * 
 * 使用场景：
 * - 任务系统的配置数据加载
 * - 任务创建和初始化
 * - 任务完成条件的验证
 * - 任务奖励的发放计算
 *
 * @author lx
 * @date 2025/06/08
 */
@ConfigTable("task.json")
public class TaskConfig extends TableConfig {
    // 任务的唯一标识符，用于区分不同的任务配置
    // 必须在整个任务系统中保持唯一性，用于任务的查找和管理
    private int id;
    
    // 任务的显示名称，用于客户端界面展示
    // 应该简洁明了，便于玩家理解任务内容
    private String name;
    
    // 任务的详细描述，说明任务的背景故事和完成方法
    // 为玩家提供任务的具体信息和指导
    private String description;
    
    // 任务类型标识，用于区分不同类别的任务
    // 例如：1-主线任务, 2-支线任务, 3-日常任务, 4-活动任务
    private int type;
    
    // 任务完成需求的配置映射
    // key为需求类型字符串，value为需求数量
    // 例如：{"kill_monster_1001": 10, "collect_item_2001": 5}
    private Map<String, Integer> requirements;
    
    // 任务完成后的物品奖励列表
    // 支持多种物品的组合奖励，每个物品包含ID和数量
    private List<ItemReward> rewards;
    
    // 任务完成后获得的经验值奖励
    // 用于角色升级和成长系统的经验累积
    private int expReward;
    
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