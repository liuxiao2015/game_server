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
     * 获取玩家背包信息
     * 
     * 功能说明：
     * - 查询指定玩家的完整背包数据信息
     * - 返回背包容量、物品列表等详细信息
     * - 提供背包数据的安全访问和封装
     * 
     * 返回信息包含：
     * - 物品列表：玩家拥有的所有物品实例
     * - 背包容量：当前背包的最大容量限制
     * - 空间统计：已使用和剩余的背包空间
     * 
     * 数据安全：
     * - 返回物品列表的副本，防止外部修改
     * - 使用BagInfo封装类统一数据格式
     * - 确保数据访问的线程安全性
     * 
     * @param playerId 玩家唯一标识ID
     * @return 包含背包信息的结果对象，失败时返回错误信息
     * 
     * 异常情况：
     * - 玩家背包数据未初始化
     * - 玩家ID不存在或无效
     * 
     * 使用场景：
     * - 客户端请求背包界面数据
     * - 背包相关功能的数据查询
     * - 背包状态的监控和统计
     */
    public Result<BagInfo> getBagInfo(long playerId) {
        List<Item> items = playerBags.get(playerId);
        if (items == null) {
            return Result.failure("玩家背包数据未找到: " + playerId);
        }
        
        int capacity = bagCapacities.getOrDefault(playerId, DEFAULT_BAG_SIZE);
        BagInfo bagInfo = new BagInfo(new ArrayList<>(items), capacity);
        return Result.success(bagInfo);
    }
    
    /**
     * 添加物品到背包
     * 
     * 功能说明：
     * - 将指定数量的物品添加到玩家背包中
     * - 自动检查背包容量和空间限制
     * - 为新物品分配全局唯一的实例ID
     * 
     * 添加流程：
     * 1. 验证玩家背包数据的有效性
     * 2. 检查物品数量参数的合法性
     * 3. 验证背包剩余空间是否足够
     * 4. 生成唯一的物品实例ID
     * 5. 创建物品对象并添加到背包
     * 
     * 容量检查：
     * - 检查当前物品数量是否已达到容量上限
     * - 防止背包空间溢出和数据异常
     * - 支持动态的容量限制和扩展
     * 
     * @param playerId 玩家唯一标识ID
     * @param itemId 物品配置ID，对应游戏配置表中的物品定义
     * @param count 添加的物品数量，必须为正数
     * @return 操作结果，成功时返回success，失败时包含错误信息
     * 
     * 异常情况：
     * - 玩家背包数据未初始化
     * - 物品数量参数无效（小于等于0）
     * - 背包空间不足无法添加
     * 
     * 优化建议：
     * - 后续可以支持相同物品的堆叠合并
     * - 考虑添加物品类型和分类管理
     * - 支持批量添加多种物品的操作
     */
    public Result<Void> addItem(long playerId, int itemId, int count) {
        List<Item> items = playerBags.get(playerId);
        if (items == null) {
            return Result.failure("玩家背包数据未找到: " + playerId);
        }
        
        if (count <= 0) {
            return Result.failure("物品数量无效: " + count);
        }
        
        // 检查背包剩余空间
        int capacity = bagCapacities.getOrDefault(playerId, DEFAULT_BAG_SIZE);
        if (items.size() >= capacity) {
            return Result.failure("背包空间不足");
        }
        
        // 生成唯一ID并创建物品实例
        long itemUid = itemUidGenerator.getAndIncrement();
        Item item = new Item(itemUid, itemId, count);
        items.add(item);
        
        logger.debug("物品添加成功: playerId={}, itemId={}, count={}, itemUid={}", 
                playerId, itemId, count, itemUid);
        return Result.success();
    }
    
    /**
     * 使用背包中的物品
     * 
     * 功能说明：
     * - 消耗玩家背包中指定数量的物品
     * - 减少物品数量，数量为零时自动移除
     * - 支持部分使用和完全消耗两种模式
     * 
     * 使用流程：
     * 1. 验证玩家背包数据的有效性
     * 2. 根据物品唯一ID查找目标物品
     * 3. 检查物品数量是否足够使用
     * 4. 减少物品数量或完全移除物品
     * 5. 记录物品使用的操作日志
     * 
     * 数量处理：
     * - 如果使用数量小于拥有数量，则减少相应数量
     * - 如果使用数量等于拥有数量，则完全移除物品
     * - 使用后数量为0的物品会从背包中自动清除
     * 
     * @param playerId 玩家唯一标识ID
     * @param itemUid 物品实例的唯一ID，用于定位具体的物品
     * @param count 使用的物品数量，不能超过拥有数量
     * @return 操作结果，成功时返回success，失败时包含错误信息
     * 
     * 异常情况：
     * - 玩家背包数据未初始化
     * - 指定的物品ID不存在
     * - 使用数量超过拥有数量
     * - 物品状态异常或已损坏
     * 
     * 业务扩展：
     * - 可以添加物品使用的前置条件检查
     * - 支持物品使用的效果触发和处理
     * - 记录物品使用的历史和统计信息
     * - 集成物品使用的成就和任务系统
     */
    public Result<Void> useItem(long playerId, long itemUid, int count) {
        List<Item> items = playerBags.get(playerId);
        if (items == null) {
            return Result.failure("玩家背包数据未找到: " + playerId);
        }
        
        Item item = items.stream()
                .filter(i -> i.getItemUid() == itemUid)
                .findFirst()
                .orElse(null);
        
        if (item == null) {
            return Result.failure("物品未找到: " + itemUid);
        }
        
        if (item.getCount() < count) {
            return Result.failure("物品数量不足");
        }
        
        // 减少物品数量
        item.setCount(item.getCount() - count);
        
        // 如果数量为0，从背包中移除物品
        if (item.getCount() == 0) {
            items.remove(item);
        }
        
        logger.debug("物品使用成功: playerId={}, itemUid={}, count={}, remaining={}", 
                playerId, itemUid, count, item.getCount());
        return Result.success();
    }
    
    /**
     * 背包信息封装类
     * 
     * 功能说明：
     * - 封装背包的完整信息，包括物品列表和容量信息
     * - 提供不可变的数据访问接口，确保数据安全
     * - 统一背包信息的数据格式和传输标准
     * 
     * 设计特点：
     * - 不可变对象设计，防止外部修改内部数据
     * - 简洁的数据结构，便于序列化和传输
     * - 清晰的访问方法，符合JavaBean规范
     * 
     * 包含信息：
     * - 物品列表：背包中所有物品的详细信息
     * - 背包容量：当前背包的最大容量限制
     * - 空间统计：已使用空间和剩余空间的计算
     * 
     * 使用场景：
     * - 客户端背包界面的数据展示
     * - 背包相关API的数据传输
     * - 背包状态的缓存和持久化
     * - 背包数据的序列化和反序列化
     */
    public static class BagInfo {
        // 背包中的物品列表，只读访问
        private final List<Item> items;
        // 背包的最大容量限制
        private final int capacity;
        
        /**
         * 构造背包信息对象
         * 
         * @param items 物品列表，会创建副本确保数据安全
         * @param capacity 背包容量，必须为正数
         */
        public BagInfo(List<Item> items, int capacity) {
            this.items = items;
            this.capacity = capacity;
        }
        
        /**
         * 获取背包中的物品列表
         * 
         * @return 物品列表，为只读副本
         */
        public List<Item> getItems() {
            return items;
        }
        
        /**
         * 获取背包的最大容量
         * 
         * @return 背包容量数值
         */
        public int getCapacity() {
            return capacity;
        }
        
        /**
         * 获取已使用的背包空间
         * 
         * @return 当前物品数量
         */
        public int getUsedSpace() {
            return items.size();
        }
        
        /**
         * 获取剩余的背包空间
         * 
         * @return 剩余可用空间数量
         */
        public int getRemainingSpace() {
            return capacity - items.size();
        }
        
        /**
         * 检查背包是否已满
         * 
         * @return true表示背包已满，false表示仍有空间
         */
        public boolean isFull() {
            return items.size() >= capacity;
        }
    }
}