package com.game.common.game;

/**
 * 游戏物品实体类
 * 
 * 功能说明：
 * - 表示游戏中的物品实例，包含物品的基本属性和状态信息
 * - 提供物品的创建、修改、查询和状态检查功能
 * - 支持物品的绑定状态和过期时间管理
 * - 作为游戏物品系统的核心数据模型
 * 
 * 设计思路：
 * - 使用唯一ID区分同类型物品的不同实例
 * - 通过配置ID关联物品的静态配置数据
 * - 支持物品的绑定机制，防止交易和转移
 * - 实现物品过期机制，支持限时物品和活动道具
 * 
 * 数据结构：
 * - itemUid: 物品实例的全局唯一标识符
 * - itemId: 物品配置表中的ID，用于获取物品属性
 * - count: 物品数量，支持堆叠和分拆
 * - bound: 绑定状态，绑定后无法交易给其他玩家
 * - expireTime: 过期时间戳，0表示永不过期
 * 
 * 使用场景：
 * - 背包系统中的物品存储和管理
 * - 交易系统中的物品交换和流通
 * - 任务系统中的奖励物品发放
 * - 商城系统中的物品购买和使用
 * 
 * 业务规则：
 * - 每个物品实例必须有唯一的itemUid
 * - 绑定状态一旦设置通常不可逆转
 * - 过期物品需要定期清理和回收
 * - 物品数量必须为正数，0数量时应删除
 *
 * @author lx
 * @date 2025/06/08
 */
public class Item {
    
    // 物品实例的全局唯一标识符，用于区分同类型物品的不同实例
    // 由系统自动生成，确保在整个游戏中的唯一性，用于物品追踪和管理
    private long itemUid;
    
    // 物品配置ID，对应游戏配置表中的物品定义
    // 用于获取物品的名称、图标、属性、使用效果等静态配置信息
    private int itemId;
    
    // 物品数量，支持物品的堆叠和分拆操作
    // 必须为正数，数量为0时表示物品应被删除
    private int count;
    
    // 物品绑定状态，标识物品是否已绑定到特定玩家
    // true表示已绑定，无法交易、邮寄或丢弃给其他玩家
    // false表示未绑定，可以自由流通和交易
    private boolean bound;
    
    // 物品过期时间戳（毫秒），0表示永不过期
    // 超过此时间的物品将被标记为过期，需要清理和回收
    // 用于实现限时物品、活动道具等功能
    private long expireTime;
    
    /**
     * 默认构造函数
     * 
     * 功能说明：
     * - 创建空的物品实例，所有字段使用默认值
     * - 主要用于序列化/反序列化和框架反射调用
     * - 使用后需要通过setter方法设置具体的物品属性
     * 
     * 注意事项：
     * - 使用此构造函数创建的对象需要后续设置必要字段
     * - 建议优先使用带参数的构造函数确保数据完整性
     */
    public Item() {}
    
    /**
     * 带参数的构造函数
     * 
     * 功能说明：
     * - 创建具有基本属性的物品实例
     * - 自动设置默认的绑定状态和过期时间
     * - 用于系统奖励发放和物品创建场景
     * 
     * 初始化逻辑：
     * - 设置物品的唯一ID、配置ID和数量
     * - 默认为未绑定状态，可自由交易
     * - 默认永不过期，适用于常规物品
     * 
     * @param itemUid 物品实例的唯一标识符，必须全局唯一
     * @param itemId 物品配置ID，对应配置表中的物品定义
     * @param count 物品数量，必须为正数
     * 
     * 使用场景：
     * - 系统奖励发放时创建物品实例
     * - 玩家获得新物品时的实例化
     * - 商城购买物品时的对象创建
     */
    public Item(long itemUid, int itemId, int count) {
        this.itemUid = itemUid;
        this.itemId = itemId;
        this.count = count;
        this.bound = false;        // 默认未绑定，可自由交易
        this.expireTime = 0;       // 默认永不过期
    }
    
    /**
     * 获取物品实例的唯一标识符
     * 
     * @return 物品的唯一ID，用于区分同类型物品的不同实例
     */
    public long getItemUid() {
        return itemUid;
    }
    
    /**
     * 设置物品实例的唯一标识符
     * 
     * 功能说明：
     * - 修改物品的唯一ID，通常在物品创建或数据迁移时使用
     * - 确保设置的ID在系统中具有唯一性
     * 
     * @param itemUid 新的物品唯一ID
     * 
     * 注意事项：
     * - 修改已存在物品的UID可能导致数据一致性问题
     * - 建议仅在系统初始化或数据修复时使用
     */
    public void setItemUid(long itemUid) {
        this.itemUid = itemUid;
    }
    
    /**
     * 获取物品的配置ID
     * 
     * @return 物品配置ID，对应配置表中的物品定义
     */
    public int getItemId() {
        return itemId;
    }
    
    /**
     * 设置物品的配置ID
     * 
     * 功能说明：
     * - 修改物品对应的配置定义
     * - 通常用于物品转换或特殊游戏机制
     * 
     * @param itemId 新的物品配置ID
     * 
     * 注意事项：
     * - 更改配置ID会改变物品的属性和行为
     * - 需要验证新配置ID的有效性
     */
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    /**
     * 获取物品的数量
     * 
     * @return 物品数量，表示该物品实例的堆叠数量
     */
    public int getCount() {
        return count;
    }
    
    /**
     * 设置物品的数量
     * 
     * 功能说明：
     * - 修改物品的堆叠数量
     * - 支持物品的使用消耗和数量增加
     * 
     * @param count 新的物品数量，必须为非负数
     * 
     * 业务规则：
     * - 数量为0时，物品应该从背包中移除
     * - 数量不能超过物品的最大堆叠限制
     * - 负数数量是无效的，应进行参数验证
     */
    public void setCount(int count) {
        this.count = count;
    }
    
    /**
     * 检查物品是否已绑定
     * 
     * @return true表示物品已绑定，无法交易；false表示可自由流通
     */
    public boolean isBound() {
        return bound;
    }
    
    /**
     * 设置物品的绑定状态
     * 
     * 功能说明：
     * - 修改物品的绑定状态，影响物品的可交易性
     * - 绑定后的物品通常无法交易、邮寄或丢弃给其他玩家
     * 
     * @param bound 绑定状态，true表示绑定，false表示未绑定
     * 
     * 业务规则：
     * - 绑定操作通常是不可逆的
     * - 某些特殊道具可能支持解绑功能
     * - 绑定状态影响物品的价值和流通性
     */
    public void setBound(boolean bound) {
        this.bound = bound;
    }
    
    /**
     * 获取物品的过期时间
     * 
     * @return 过期时间戳（毫秒），0表示永不过期
     */
    public long getExpireTime() {
        return expireTime;
    }
    
    /**
     * 设置物品的过期时间
     * 
     * 功能说明：
     * - 设置物品的有效期限，实现限时物品功能
     * - 过期后的物品将被系统自动清理和回收
     * 
     * @param expireTime 过期时间戳（毫秒），0表示永不过期
     * 
     * 使用场景：
     * - 活动期间发放的限时道具
     * - 试用期物品和体验道具
     * - 防止物品长期占用存储空间
     */
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
    
    /**
     * 检查物品是否已过期
     * 
     * 功能说明：
     * - 根据当前时间判断物品是否超过有效期
     * - 为物品清理和过期处理提供依据
     * - 支持限时物品和活动道具的时效管理
     * 
     * 判定逻辑：
     * 1. 如果过期时间为0，表示永不过期，返回false
     * 2. 如果当前时间超过过期时间，表示已过期，返回true
     * 3. 否则物品仍在有效期内，返回false
     * 
     * @return true表示物品已过期，需要清理；false表示物品仍有效
     * 
     * 使用场景：
     * - 背包系统的定期清理任务
     * - 物品使用前的有效性检查
     * - 交易系统的物品状态验证
     * - 系统维护时的数据清理
     * 
     * 性能考虑：
     * - 使用系统当前时间进行比较，避免复杂计算
     * - 可以考虑缓存结果，减少重复计算
     * - 批量检查时可以优化为单次时间获取
     */
    public boolean isExpired() {
        return expireTime > 0 && System.currentTimeMillis() > expireTime;
    }
}