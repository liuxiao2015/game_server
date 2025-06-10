package com.game.service.logic.module.bag;

import com.game.common.game.Item;
import com.game.common.game.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 背包模块
 * 
 * 功能说明：
 * - 负责游戏背包系统的核心数据管理和业务逻辑
 * - 提供背包的初始化、物品增删、容量控制等功能
 * - 支持物品的使用、查询、整理等常用操作
 * - 管理玩家物品数据的存储和访问
 * 
 * 设计思路：
 * - 采用内存存储提供高性能的物品操作
 * - 使用线程安全的数据结构支持并发访问
 * - 通过物品唯一ID区分同类型的不同物品实例
 * - 提供灵活的容量管理和扩展机制
 * 
 * 核心功能：
 * - 背包初始化：为新玩家创建背包数据和设置默认容量
 * - 物品管理：添加、使用、查询背包中的物品
 * - 容量控制：检查背包空间，防止超出容量限制
 * - 数据查询：获取背包的完整信息和统计数据
 * 
 * 数据结构：
 * - 物品列表：存储玩家拥有的所有物品实例
 * - 容量映射：记录每个玩家的背包最大容量
 * - 唯一ID：为每个物品实例分配全局唯一标识
 * 
 * 使用场景：
 * - 游戏奖励发放和物品获得
 * - 玩家背包界面的数据展示
 * - 物品使用和消耗的处理
 * - 背包整理和容量扩展
 *
 * @author lx 
 * @date 2025/06/08
 */
public class BagModule {
    
    // 日志记录器，用于记录背包操作的关键信息和调试数据
    private static final Logger logger = LoggerFactory.getLogger(BagModule.class);
    // 默认背包容量，新玩家初始化时的标准背包大小
    private static final int DEFAULT_BAG_SIZE = 100;
    // 物品唯一ID生成器，确保每个物品实例都有全局唯一的标识
    private static final AtomicLong itemUidGenerator = new AtomicLong(1);
    
    // 玩家背包数据存储：玩家ID -> 物品列表
    // 使用 ConcurrentHashMap 保证多线程环境下的数据安全
    private final ConcurrentHashMap<Long, List<Item>> playerBags = new ConcurrentHashMap<>();
    // 玩家背包容量配置：玩家ID -> 背包容量
    // 支持个性化的背包容量设置和动态扩展
    private final ConcurrentHashMap<Long, Integer> bagCapacities = new ConcurrentHashMap<>();
    
    /**
     * 初始化玩家背包数据
     * 
     * 功能说明：
     * - 为指定玩家创建全新的背包数据结构
     * - 初始化空的物品列表和默认的背包容量
     * - 确保背包数据的正确初始化和内存分配
     * 
     * 初始化内容：
     * - 创建空的物品列表（ArrayList）用于存储物品
     * - 设置默认背包容量为100个物品槽位
     * - 建立玩家ID与背包数据的映射关系
     * 
     * 调用时机：
     * - 玩家首次登录游戏时
     * - 玩家数据重置或清空时
     * - 服务器重启后的数据恢复时
     * 
     * @param playerId 玩家唯一标识ID
     * 
     * 注意事项：
     * - 如果玩家已有背包数据，会被重新初始化（慎用）
     * - 初始化后的背包为空，需要从数据库加载历史数据
     * - 该操作是线程安全的，支持并发调用
     */
    public void initPlayerBag(long playerId) {
        playerBags.put(playerId, new ArrayList<>());
        bagCapacities.put(playerId, DEFAULT_BAG_SIZE);
        logger.info("初始化玩家背包: playerId={}, capacity={}", playerId, DEFAULT_BAG_SIZE);
    }
    
    /**
     * 获取背包信息
     */
    public Result<BagInfo> getBagInfo(long playerId) {
        List<Item> items = playerBags.get(playerId);
        if (items == null) {
            return Result.failure("Player bag not found: " + playerId);
        }
        
        int capacity = bagCapacities.getOrDefault(playerId, DEFAULT_BAG_SIZE);
        BagInfo bagInfo = new BagInfo(new ArrayList<>(items), capacity);
        return Result.success(bagInfo);
    }
    
    /**
     * 添加物品到背包
     */
    public Result<Void> addItem(long playerId, int itemId, int count) {
        List<Item> items = playerBags.get(playerId);
        if (items == null) {
            return Result.failure("Player bag not found: " + playerId);
        }
        
        if (count <= 0) {
            return Result.failure("Invalid item count: " + count);
        }
        
        // 检查背包空间
        int capacity = bagCapacities.getOrDefault(playerId, DEFAULT_BAG_SIZE);
        if (items.size() >= capacity) {
            return Result.failure("Bag is full");
        }
        
        // 生成唯一ID并添加物品
        long itemUid = itemUidGenerator.getAndIncrement();
        Item item = new Item(itemUid, itemId, count);
        items.add(item);
        
        logger.debug("Added item to bag: playerId={}, itemId={}, count={}", playerId, itemId, count);
        return Result.success();
    }
    
    /**
     * 使用物品
     */
    public Result<Void> useItem(long playerId, long itemUid, int count) {
        List<Item> items = playerBags.get(playerId);
        if (items == null) {
            return Result.failure("Player bag not found: " + playerId);
        }
        
        Item item = items.stream()
                .filter(i -> i.getItemUid() == itemUid)
                .findFirst()
                .orElse(null);
        
        if (item == null) {
            return Result.failure("Item not found: " + itemUid);
        }
        
        if (item.getCount() < count) {
            return Result.failure("Insufficient item count");
        }
        
        // 减少物品数量
        item.setCount(item.getCount() - count);
        
        // 如果数量为0，移除物品
        if (item.getCount() == 0) {
            items.remove(item);
        }
        
        logger.debug("Used item: playerId={}, itemUid={}, count={}", playerId, itemUid, count);
        return Result.success();
    }
    
    /**
     * 背包信息类
     */
    public static class BagInfo {
        private final List<Item> items;
        private final int capacity;
        
        public BagInfo(List<Item> items, int capacity) {
            this.items = items;
            this.capacity = capacity;
        }
        
        public List<Item> getItems() {
            return items;
        }
        
        public int getCapacity() {
            return capacity;
        }
    }
}