package com.game.common.config;

import java.util.Map;

/**
 * 游戏物品配置类
 * 
 * 功能说明：
 * - 定义游戏中所有物品的基础属性和配置参数
 * - 支持从JSON配置文件动态加载物品数据
 * - 提供物品类型、品质、属性、堆叠等完整的配置管理
 * - 为游戏的物品系统提供数据基础和规则支撑
 * 
 * 数据来源：
 * - 配置文件：item.json，包含所有物品的配置数据
 * - 支持热更新：配置文件修改后可动态重载
 * - 数据验证：加载时进行完整性和合法性检查
 * - 版本管理：支持配置版本号和向后兼容
 * 
 * 配置项说明：
 * - id：物品唯一标识，全局唯一的物品ID
 * - name：物品名称，用于游戏内显示和搜索
 * - type：物品类型，决定物品的功能分类和使用方式
 * - quality：物品品质等级，影响属性和稀有度
 * - icon：物品图标资源路径，用于UI显示
 * - attributes：物品属性映射，包含各种数值属性
 * - stackable：是否可堆叠，影响背包存储方式
 * - maxStack：最大堆叠数量，堆叠物品的数量上限
 * 
 * 物品类型定义：
 * - 1：装备类（武器、防具、饰品等）
 * - 2：消耗品（药品、食物、道具等）
 * - 3：材料类（制作材料、升级石等）
 * - 4：任务道具（剧情物品、特殊道具等）
 * - 5：货币类（金币、钻石、积分等）
 * 
 * 品质等级：
 * - 1：普通（白色）
 * - 2：优秀（绿色）
 * - 3：稀有（蓝色）
 * - 4：史诗（紫色）
 * - 5：传说（橙色）
 * - 6：神话（红色）
 * 
 * 属性系统：
 * - attack：攻击力属性值
 * - defense：防御力属性值
 * - health：生命值属性加成
 * - mana：魔法值属性加成
 * - speed：速度属性加成
 * - critical：暴击率属性加成
 * 
 * 使用场景：
 * - 物品系统的数据基础
 * - 背包管理的配置依据
 * - 商店系统的商品信息
 * - 战斗系统的装备属性计算
 * - 任务系统的奖励物品配置
 * 
 * 性能优化：
 * - 配置数据缓存在内存中，避免频繁IO操作
 * - 支持按物品类型和品质的快速索引查询
 * - 提供批量查询接口，减少单次查询开销
 * 
 * 扩展性考虑：
 * - attributes使用Map结构，支持动态扩展新属性
 * - 配置格式向前兼容，支持新版本字段添加
 * - 支持自定义物品类型和品质的扩展
 *
 * @author lx
 * @date 2025/06/08
 */
@ConfigTable("item.json")  // 指定配置文件路径，支持自动加载和解析
public class ItemConfig extends TableConfig {
    
    /** 物品唯一标识ID，全局唯一，用于物品的识别和引用 */
    private int id;
    
    /** 物品名称，支持多语言，用于游戏内显示和玩家识别 */
    private String name;
    
    /** 物品类型，决定物品的功能分类：1-装备，2-消耗品，3-材料，4-任务道具，5-货币 */
    private int type;
    
    /** 物品品质等级，影响属性强度和稀有度：1-普通，2-优秀，3-稀有，4-史诗，5-传说，6-神话 */
    private int quality;
    
    /** 物品图标资源路径，用于UI界面的图标显示 */
    private String icon;
    
    /** 物品属性映射表，包含攻击力、防御力、生命值等各种数值属性 */
    private Map<String, Integer> attributes;
    
    /** 是否支持堆叠存储，true表示可以多个物品占用一个背包格子 */
    private boolean stackable;
    
    /** 最大堆叠数量，仅当stackable为true时有效，限制单个格子的最大数量 */
    private int maxStack;
    
    /**
     * 获取物品唯一标识ID
     * 
     * @return 物品ID，用于全局唯一标识和引用
     */
    @Override
    public int getId() {
        return id;
    }
    
    /**
     * 设置物品ID
     * 
     * @param id 物品唯一标识，必须保证全局唯一性
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * 获取物品名称
     * 
     * @return 物品显示名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置物品名称
     * 
     * @param name 物品名称，用于游戏内显示
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取物品类型
     * 
     * @return 物品类型码：1-装备，2-消耗品，3-材料，4-任务道具，5-货币
     */
    public int getType() {
        return type;
    }
    
    /**
     * 设置物品类型
     * 
     * @param type 物品类型标识，决定物品的功能分类
     */
    public void setType(int type) {
        this.type = type;
    }
    
    /**
     * 获取物品品质等级
     * 
     * @return 品质等级：1-普通，2-优秀，3-稀有，4-史诗，5-传说，6-神话
     */
    public int getQuality() {
        return quality;
    }
    
    /**
     * 设置物品品质等级
     * 
     * @param quality 品质等级，影响物品的稀有度和属性强度
     */
    public void setQuality(int quality) {
        this.quality = quality;
    }
    
    /**
     * 获取物品图标路径
     * 
     * @return 图标资源路径，用于UI显示
     */
    public String getIcon() {
        return icon;
    }
    
    /**
     * 设置物品图标路径
     * 
     * @param icon 图标资源的相对路径或URL
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    /**
     * 获取物品属性映射
     * 
     * @return 属性键值对，包含攻击力、防御力等各种数值属性
     */
    public Map<String, Integer> getAttributes() {
        return attributes;
    }
    
    /**
     * 设置物品属性映射
     * 
     * @param attributes 属性映射表，key为属性名，value为属性值
     */
    public void setAttributes(Map<String, Integer> attributes) {
        this.attributes = attributes;
    }
    
    /**
     * 检查物品是否支持堆叠
     * 
     * 功能说明：
     * - 返回物品是否可以在背包中堆叠存储
     * - 堆叠物品可以节省背包空间，提升存储效率
     * - 通常消耗品和材料类物品支持堆叠
     * - 装备类物品通常不支持堆叠，因为每件装备可能有不同属性
     * 
     * 业务规则：
     * - 堆叠物品在背包中占用一个格子，显示总数量
     * - 使用时优先消耗最早获得的物品（先进先出）
     * - 达到最大堆叠数量时，新获得的物品会占用新格子
     * 
     * @return true表示可堆叠，false表示每个物品独占一个格子
     */
    public boolean isStackable() {
        return stackable;
    }
    
    /**
     * 设置物品堆叠属性
     * 
     * @param stackable 是否支持堆叠存储
     */
    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }
    
    /**
     * 获取最大堆叠数量
     * 
     * @return 单个背包格子的最大堆叠数量，仅当stackable为true时有效
     */
    public int getMaxStack() {
        return maxStack;
    }
    
    /**
     * 设置最大堆叠数量
     * 
     * @param maxStack 堆叠上限，超过此数量需要占用新的背包格子
     */
    public void setMaxStack(int maxStack) {
        this.maxStack = maxStack;
    }
}