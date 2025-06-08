package com.game.frame.data.utils;

/**
 * 缓存键生成器
 * @author lx
 * @date 2025/06/08
 */
public class CacheKeyGenerator {

    private static final String SEPARATOR = ":";

    /**
     * 生成用户缓存键
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String userKey(Long userId) {
        return "user" + SEPARATOR + userId;
    }

    /**
     * 生成用户名缓存键
     * @param username 用户名
     * @return 缓存键
     */
    public static String usernameKey(String username) {
        return "username" + SEPARATOR + username;
    }

    /**
     * 生成物品缓存键
     * @param itemId 物品ID
     * @return 缓存键
     */
    public static String itemKey(Long itemId) {
        return "item" + SEPARATOR + itemId;
    }

    /**
     * 生成用户物品缓存键
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String userItemsKey(Long userId) {
        return "user_items" + SEPARATOR + userId;
    }

    /**
     * 生成用户物品类型缓存键
     * @param userId 用户ID
     * @param itemType 物品类型
     * @return 缓存键
     */
    public static String userItemTypeKey(Long userId, String itemType) {
        return "user_items" + SEPARATOR + userId + SEPARATOR + itemType;
    }

    /**
     * 生成玩家数据缓存键
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String playerDataKey(Long userId) {
        return "player_data" + SEPARATOR + userId;
    }

    /**
     * 生成通用实体缓存键
     * @param entityType 实体类型
     * @param id 实体ID
     * @return 缓存键
     */
    public static String entityKey(String entityType, Object id) {
        return entityType + SEPARATOR + id;
    }

    /**
     * 生成查询结果缓存键
     * @param queryName 查询名称
     * @param params 参数
     * @return 缓存键
     */
    public static String queryKey(String queryName, Object... params) {
        StringBuilder key = new StringBuilder("query").append(SEPARATOR).append(queryName);
        for (Object param : params) {
            key.append(SEPARATOR).append(param);
        }
        return key.toString();
    }

    /**
     * 生成统计缓存键
     * @param statType 统计类型
     * @param params 参数
     * @return 缓存键
     */
    public static String statKey(String statType, Object... params) {
        StringBuilder key = new StringBuilder("stat").append(SEPARATOR).append(statType);
        for (Object param : params) {
            key.append(SEPARATOR).append(param);
        }
        return key.toString();
    }
}