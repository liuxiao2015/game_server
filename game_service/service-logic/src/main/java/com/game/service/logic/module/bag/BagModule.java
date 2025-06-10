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
 * 负责背包初始化、物品管理、容量控制
 *
 * @author lx
 * @date 2025/06/08
 */
public class BagModule {
    
    private static final Logger logger = LoggerFactory.getLogger(BagModule.class);
    private static final int DEFAULT_BAG_SIZE = 100;
    private static final AtomicLong itemUidGenerator = new AtomicLong(1);
    
    // 玩家背包数据 playerId -> List<Item>
    private final ConcurrentHashMap<Long, List<Item>> playerBags = new ConcurrentHashMap<>();
    // 玩家背包容量 playerId -> capacity
    private final ConcurrentHashMap<Long, Integer> bagCapacities = new ConcurrentHashMap<>();
    
    /**
     * 初始化玩家背包
     */
    public void initPlayerBag(long playerId) {
        playerBags.put(playerId, new ArrayList<>());
        bagCapacities.put(playerId, DEFAULT_BAG_SIZE);
        logger.info("Initialized bag for player: {}", playerId);
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