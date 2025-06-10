package com.game.common.game;

/**
 * 游戏物品实体类
 * 
 * 功能说明：
 * - 表示游戏中的物品数据模型和属性信息
 * - 封装物品的基础属性和状态管理
 * - 支持物品的唯一标识和配置关联
 * - 提供物品有效期和绑定状态的管理
 * 
 * 设计思路：
 * - 采用JavaBean模式，提供标准的getter/setter方法
 * - 区分物品实例ID和配置ID，支持同类型物品的多实例
 * - 支持物品绑定机制，限制物品的交易和转移
 * - 集成过期时间管理，支持限时物品的自动清理
 * 
 * 核心属性：
 * - 实例ID：每个物品实例的全局唯一标识
 * - 配置ID：物品类型的配置表ID，关联物品属性
 * - 数量：可堆叠物品的数量信息
 * - 绑定状态：物品是否绑定，影响交易和转移
 * - 过期时间：物品的有效期限制
 * 
 * 业务场景：
 * - 背包系统：物品的存储和管理
 * - 交易系统：物品的转移和交换
 * - 任务系统：任务奖励和消耗物品
 * - 商城系统：物品的购买和销售
 * 
 * 使用场景：
 * - 玩家背包中的所有物品实例
 * - 游戏奖励和掉落的物品生成
 * - 物品使用和消耗的处理
 * - 物品交易和转移的数据传输
 *
 * @author lx
 * @date 2025/06/08
 */
public class Item {
    
    // 物品实例的全局唯一标识ID，用于区分同类型的不同物品实例
    private long itemUid;
    // 物品配置ID，对应游戏配置表中的物品定义，决定物品的基础属性
    private int itemId;
    // 物品数量，支持可堆叠物品的数量管理
    private int count;
    // 物品绑定状态，绑定物品无法交易、转移或删除
    private boolean bound;
    // 物品过期时间戳，0表示永不过期，大于0表示具体的过期时间
    private long expireTime;
    
    /**
     * 默认构造函数
     * 创建空的物品实例，所有属性使用默认值
     */
    public Item() {}
    
    /**
     * 构造物品实例
     * 
     * @param itemUid 物品实例的唯一ID
     * @param itemId 物品配置ID，对应配置表中的物品定义
     * @param count 物品数量，必须为正数
     */
    public Item(long itemUid, int itemId, int count) {
        this.itemUid = itemUid;
        this.itemId = itemId;
        this.count = count;
        this.bound = false;     // 默认为非绑定状态
        this.expireTime = 0;    // 默认永不过期
    }
    
    public long getItemUid() {
        return itemUid;
    }
    
    public void setItemUid(long itemUid) {
        this.itemUid = itemUid;
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
    
    public boolean isBound() {
        return bound;
    }
    
    public void setBound(boolean bound) {
        this.bound = bound;
    }
    
    public long getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
    
    /**
     * 检查物品是否已过期
     * 
     * 功能说明：
     * - 检查当前物品是否超过了有效期限
     * - 用于物品使用前的有效性验证
     * - 支持定时清理任务的过期物品筛选
     * 
     * 判断逻辑：
     * - 如果过期时间为0，表示永不过期，返回false
     * - 如果过期时间大于0且小于当前时间，表示已过期，返回true
     * - 否则物品仍在有效期内，返回false
     * 
     * @return true表示物品已过期，false表示物品仍有效
     * 
     * 使用场景：
     * - 物品使用前的有效性检查
     * - 背包整理时的过期物品清理
     * - 定时任务的批量过期物品处理
     * - 物品交易时的有效性验证
     */
    public boolean isExpired() {
        return expireTime > 0 && System.currentTimeMillis() > expireTime;
    }
}