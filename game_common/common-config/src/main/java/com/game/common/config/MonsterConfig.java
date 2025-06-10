package com.game.common.config;

import java.util.List;
import java.util.Map;

/**
 * 游戏怪物配置类
 * 
 * 功能说明：
 * - 定义游戏中所有怪物的基础属性和行为配置
 * - 包含怪物的战斗属性、技能系统和掉落奖励设置
 * - 支持灵活的属性配置和多样化的掉落机制
 * - 为战斗系统和奖励系统提供数据支撑
 * 
 * 设计思路：
 * - 继承TableConfig实现统一的配置管理
 * - 使用@ConfigTable注解关联monster.json配置文件
 * - 采用Map结构支持可扩展的属性系统
 * - 提供结构化的掉落配置支持概率和数量控制
 * 
 * 配置结构：
 * - 基本信息：怪物ID、名称、等级
 * - 属性系统：HP、攻击力、防御力等战斗属性
 * - 技能系统：怪物可使用的技能列表
 * - 掉落系统：战斗胜利后的物品掉落配置
 * 
 * 属性系统：
 * - 使用Map结构支持动态属性扩展
 * - 常见属性：hp(生命值), attack(攻击力), defense(防御力)
 * - 支持特殊属性：critical_rate(暴击率), dodge_rate(闪避率)
 * - 属性值为整数类型，便于战斗计算
 * 
 * 技能系统：
 * - 技能列表存储技能的标识字符串
 * - 支持被动技能和主动技能的配置
 * - 技能效果通过技能系统独立实现
 * 
 * 掉落系统：
 * - 支持多物品同时掉落配置
 * - 每个物品有独立的掉落概率和数量范围
 * - 支持稀有物品的低概率掉落机制
 * 
 * 使用场景：
 * - 战斗系统中怪物实例的创建和初始化
 * - 战斗过程中怪物属性的查询和计算
 * - 战斗结束后掉落物品的生成和分配
 * - 游戏平衡性调整和数值优化
 *
 * @author lx
 * @date 2025/06/08
 */
@ConfigTable("monster.json")
public class MonsterConfig extends TableConfig {
    // 怪物的唯一标识符，用于区分不同种类的怪物
    // 在整个游戏中必须保持唯一性，用于怪物配置查找和实例创建
    private int id;
    
    // 怪物的显示名称，用于客户端界面显示和战斗日志
    // 应该具有辨识度，便于玩家识别不同类型的怪物
    private String name;
    
    // 怪物的等级，影响基础属性和战斗难度
    // 用于战斗匹配、经验计算和奖励评估
    private int level;
    
    // 怪物的属性配置映射表
    // key为属性名称，value为属性数值
    // 常见属性：hp(生命值), attack(攻击力), defense(防御力), speed(速度)
    private Map<String, Integer> attributes;
    
    // 怪物可使用的技能标识列表
    // 存储技能的字符串标识，具体技能效果由技能系统处理
    private List<String> skills;
    
    // 怪物的掉落物品配置列表
    // 包含物品ID、掉落概率和数量范围等信息
    private List<DropItem> drops;
    
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
     * 怪物掉落物品配置类
     * 
     * 功能说明：
     * - 定义单个掉落物品的完整配置信息
     * - 支持概率性掉落和数量随机化
     * - 为战斗奖励系统提供掉落计算依据
     * 
     * 设计特点：
     * - 独立的掉落配置，支持精细化控制
     * - 概率范围0.0-1.0，便于掉落判定
     * - 数量范围配置，增加掉落的随机性
     * 
     * 掉落机制：
     * - 首先进行概率判定，决定是否掉落
     * - 然后在最小-最大数量范围内随机确定掉落数量
     * - 支持必掉物品（概率1.0）和稀有物品（低概率）
     * 
     * 使用场景：
     * - 怪物击败后的战利品生成
     * - 副本奖励和活动掉落配置
     * - 经济平衡和物品稀缺度控制
     */
    public static class DropItem {
        // 掉落物品的配置ID，对应游戏物品配置表
        private int itemId;
        
        // 掉落概率，范围为0.0-1.0
        // 1.0表示100%掉落，0.1表示10%掉落概率
        private float rate;
        
        // 掉落物品的最小数量
        // 当触发掉落时，至少掉落的物品数量
        private int minCount;
        
        // 掉落物品的最大数量
        // 当触发掉落时，最多掉落的物品数量
        private int maxCount;
        
        /**
         * 默认构造函数
         * 用于JSON反序列化和框架创建实例
         */
        public DropItem() {}
        
        /**
         * 完整参数构造函数
         * 
         * @param itemId 物品配置ID
         * @param rate 掉落概率(0.0-1.0)
         * @param minCount 最小掉落数量
         * @param maxCount 最大掉落数量
         */
        public DropItem(int itemId, float rate, int minCount, int maxCount) {
            this.itemId = itemId;
            this.rate = rate;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
        
        /**
         * 获取掉落物品的配置ID
         * 
         * @return 物品配置ID
         */
        public int getItemId() {
            return itemId;
        }
        
        /**
         * 设置掉落物品的配置ID
         * 
         * @param itemId 物品配置ID
         */
        public void setItemId(int itemId) {
            this.itemId = itemId;
        }
        
        /**
         * 获取掉落概率
         * 
         * @return 掉落概率(0.0-1.0)
         */
        public float getRate() {
            return rate;
        }
        
        /**
         * 设置掉落概率
         * 
         * @param rate 掉落概率，应在0.0-1.0范围内
         */
        public void setRate(float rate) {
            this.rate = rate;
        }
        
        /**
         * 获取最小掉落数量
         * 
         * @return 最小掉落数量
         */
        public int getMinCount() {
            return minCount;
        }
        
        /**
         * 设置最小掉落数量
         * 
         * @param minCount 最小掉落数量，应大于0
         */
        public void setMinCount(int minCount) {
            this.minCount = minCount;
        }
        
        /**
         * 获取最大掉落数量
         * 
         * @return 最大掉落数量
         */
        public int getMaxCount() {
            return maxCount;
        }
        
        /**
         * 设置最大掉落数量
         * 
         * @param maxCount 最大掉落数量，应大于等于最小数量
         */
        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }
    }
}