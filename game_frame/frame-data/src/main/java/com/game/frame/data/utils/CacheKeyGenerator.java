package com.game.frame.data.utils;

/**
 * 缓存键生成工具类
 * 
 * 功能说明：
 * - 提供统一的缓存键生成规范，确保缓存键的一致性和唯一性
 * - 支持多种业务场景的缓存键生成，覆盖用户、物品、查询等场景
 * - 采用可读性良好的命名规范，便于缓存监控和调试
 * - 避免缓存键冲突，确保缓存数据的准确性
 * 
 * 设计思路：
 * - 使用静态方法提供工具类功能，无需实例化
 * - 统一使用冒号(:)作为分隔符，符合Redis键命名规范
 * - 采用前缀+标识的模式，确保不同类型数据的键不冲突
 * - 支持参数化键生成，适应复杂的业务场景
 * 
 * 键命名规范：
 * - 格式：{类型}:{标识}:{子标识}...
 * - 示例：user:123、item:456、user_items:123:equipment
 * - 前缀明确标识数据类型，便于分类管理
 * - 层次结构清晰，支持键的模糊匹配和批量操作
 * 
 * 使用场景：
 * - 用户数据缓存：用户信息、登录状态等
 * - 游戏物品缓存：物品详情、用户背包等
 * - 查询结果缓存：复杂查询、统计结果等
 * - 业务配置缓存：游戏配置、系统设置等
 * 
 * 技术特点：
 * - 类型安全：通过方法重载支持不同参数类型
 * - 性能优化：使用StringBuilder避免字符串拼接开销
 * - 扩展性强：易于添加新的键生成方法
 * - 调试友好：生成的键具有良好的可读性
 * 
 * 缓存策略：
 * - 用户相关：基于用户ID，支持用户维度的缓存管理
 * - 实体相关：基于实体类型和ID，支持通用实体缓存
 * - 查询相关：基于查询条件，支持查询结果缓存
 * - 统计相关：基于统计维度，支持统计数据缓存
 * 
 * @author lx
 * @date 2025/06/08
 */
public class CacheKeyGenerator {

    /**
     * 键分隔符常量
     * 使用冒号作为分隔符，符合Redis键命名最佳实践
     */
    private static final String SEPARATOR = ":";

    /**
     * 生成用户信息缓存键
     * 
     * 功能说明：
     * - 为用户基本信息生成缓存键
     * - 用于缓存用户详情、个人资料等数据
     * - 格式：user:{userId}
     * 
     * 使用场景：
     * - 用户登录后的信息缓存
     * - 用户资料查询的结果缓存
     * - 用户权限和状态信息缓存
     * 
     * @param userId 用户ID，不能为null
     * @return String 用户缓存键，格式为"user:{userId}"
     */
    public static String userKey(Long userId) {
        return "user" + SEPARATOR + userId;
    }

    /**
     * 生成用户名映射缓存键
     * 
     * 功能说明：
     * - 为用户名到用户ID的映射生成缓存键
     * - 用于登录时根据用户名快速查找用户
     * - 格式：username:{username}
     * 
     * 使用场景：
     * - 用户登录验证时的用户名查找
     * - 用户名唯一性检查
     * - 用户搜索和匹配功能
     * 
     * @param username 用户名，不能为null或空
     * @return String 用户名缓存键，格式为"username:{username}"
     */
    public static String usernameKey(String username) {
        return "username" + SEPARATOR + username;
    }

    /**
     * 生成单个物品信息缓存键
     * 
     * 功能说明：
     * - 为单个物品实体生成缓存键
     * - 用于缓存物品的详细信息和属性
     * - 格式：item:{itemId}
     * 
     * 使用场景：
     * - 物品详情页面的数据缓存
     * - 物品属性和配置信息缓存
     * - 物品操作时的数据预加载
     * 
     * @param itemId 物品ID，不能为null
     * @return String 物品缓存键，格式为"item:{itemId}"
     */
    public static String itemKey(Long itemId) {
        return "item" + SEPARATOR + itemId;
    }

    /**
     * 生成用户物品列表缓存键
     * 
     * 功能说明：
     * - 为用户的所有物品列表生成缓存键
     * - 用于缓存用户背包、仓库等物品集合
     * - 格式：user_items:{userId}
     * 
     * 使用场景：
     * - 背包系统的物品列表展示
     * - 用户物品统计和管理
     * - 物品交易前的库存检查
     * 
     * @param userId 用户ID，不能为null
     * @return String 用户物品缓存键，格式为"user_items:{userId}"
     */
    public static String userItemsKey(Long userId) {
        return "user_items" + SEPARATOR + userId;
    }

    /**
     * 生成用户特定类型物品缓存键
     * 
     * 功能说明：
     * - 为用户的特定类型物品生成缓存键
     * - 支持按物品类型分类缓存，提升查询效率
     * - 格式：user_items:{userId}:{itemType}
     * 
     * 使用场景：
     * - 装备栏的装备列表缓存
     * - 道具包的道具列表缓存
     * - 材料包的材料列表缓存
     * - 按类型统计用户物品数量
     * 
     * @param userId 用户ID，不能为null
     * @param itemType 物品类型，如"equipment"、"consumable"等
     * @return String 用户特定类型物品缓存键
     */
    public static String userItemTypeKey(Long userId, String itemType) {
        return "user_items" + SEPARATOR + userId + SEPARATOR + itemType;
    }

    /**
     * 生成玩家游戏数据缓存键
     * 
     * 功能说明：
     * - 为玩家的游戏数据生成缓存键
     * - 用于缓存等级、经验、技能等游戏相关数据
     * - 格式：player_data:{userId}
     * 
     * 使用场景：
     * - 玩家游戏状态的实时缓存
     * - 游戏大厅的玩家信息展示
     * - 游戏匹配时的玩家数据查询
     * 
     * @param userId 用户ID，不能为null
     * @return String 玩家数据缓存键，格式为"player_data:{userId}"
     */
    public static String playerDataKey(Long userId) {
        return "player_data" + SEPARATOR + userId;
    }

    /**
     * 生成通用实体缓存键
     * 
     * 功能说明：
     * - 为任意类型的实体生成通用缓存键
     * - 支持扩展，适用于各种业务实体
     * - 格式：{entityType}:{id}
     * 
     * 使用场景：
     * - 通用的实体缓存框架
     * - 动态实体类型的缓存支持
     * - 第三方实体的缓存集成
     * 
     * @param entityType 实体类型名称，如"order"、"payment"等
     * @param id 实体ID，支持各种类型的ID
     * @return String 通用实体缓存键
     */
    public static String entityKey(String entityType, Object id) {
        return entityType + SEPARATOR + id;
    }

    /**
     * 生成查询结果缓存键
     * 
     * 功能说明：
     * - 为复杂查询的结果生成缓存键
     * - 支持多参数查询条件的键生成
     * - 格式：query:{queryName}:{param1}:{param2}...
     * 
     * 使用场景：
     * - 复杂查询结果的缓存
     * - 报表查询的结果缓存
     * - 多条件筛选的结果缓存
     * - API查询结果的缓存
     * 
     * 实现细节：
     * - 使用StringBuilder优化字符串拼接性能
     * - 支持可变参数，适应不同查询条件数量
     * - 参数顺序影响键值，需要保证参数顺序一致性
     * 
     * @param queryName 查询名称，标识查询类型
     * @param params 查询参数列表，支持多个参数
     * @return String 查询结果缓存键
     */
    public static String queryKey(String queryName, Object... params) {
        StringBuilder key = new StringBuilder("query").append(SEPARATOR).append(queryName);
        for (Object param : params) {
            key.append(SEPARATOR).append(param);
        }
        return key.toString();
    }

    /**
     * 生成统计数据缓存键
     * 
     * 功能说明：
     * - 为统计计算结果生成缓存键
     * - 支持多维度统计的键生成
     * - 格式：stat:{statType}:{param1}:{param2}...
     * 
     * 使用场景：
     * - 用户统计数据的缓存
     * - 游戏数据统计的缓存
     * - 业务指标统计的缓存
     * - 实时监控数据的缓存
     * 
     * 统计类型示例：
     * - "user_count"：用户数量统计
     * - "item_total"：物品总量统计
     * - "daily_active"：日活跃用户统计
     * - "revenue_sum"：收入汇总统计
     * 
     * @param statType 统计类型，标识统计指标
     * @param params 统计参数列表，如时间范围、分组条件等
     * @return String 统计数据缓存键
     */
    public static String statKey(String statType, Object... params) {
        StringBuilder key = new StringBuilder("stat").append(SEPARATOR).append(statType);
        for (Object param : params) {
            key.append(SEPARATOR).append(param);
        }
        return key.toString();
    }
}