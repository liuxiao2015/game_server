package com.game.service.logic.module;

import com.game.common.game.Result;
import com.game.service.logic.module.bag.BagModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 背包模块测试类
 * 
 * 功能说明：
 * - 验证背包模块的核心功能正确性和稳定性
 * - 测试物品添加、使用、查询等基础操作
 * - 确保背包数据的一致性和业务逻辑的正确性
 * - 提供回归测试保障，防止背包功能回退
 * 
 * 测试范围：
 * - 物品添加功能：验证物品正确添加到玩家背包
 * - 物品使用功能：测试物品使用和数量扣除
 * - 背包查询功能：验证背包信息的正确性
 * - 边界条件测试：使用全部物品、空背包等场景
 * 
 * 测试策略：
 * - 使用JUnit 5框架进行单元测试
 * - 每个测试方法独立运行，确保测试隔离
 * - 使用断言验证业务逻辑的正确性
 * - 模拟真实玩家操作，提高测试的实用性
 * 
 * 业务场景：
 * - 玩家获得物品后的背包更新
 * - 玩家使用消耗品的数量变化
 * - 物品用尽后的自动清理机制
 * - 背包容量和物品管理
 * 
 * 质量保障：
 * - 验证返回结果的成功状态
 * - 检查数据变更的准确性
 * - 确保业务规则的一致性执行
 * - 防止数据异常和逻辑错误
 *
 * @author lx
 * @date 2025/06/08
 */
public class BagModuleTest {
    
    /** 背包模块实例，用于执行业务操作和功能测试 */
    private BagModule bagModule;
    
    /** 测试用玩家ID，模拟真实玩家进行背包操作 */
    private static final long TEST_PLAYER_ID = 123L;
    
    /**
     * 测试环境初始化方法
     * 
     * 功能说明：
     * - 在每个测试方法执行前准备测试环境
     * - 创建背包模块实例并初始化玩家背包
     * - 确保每个测试都有干净的初始状态
     * 
     * 初始化步骤：
     * 1. 创建背包模块实例
     * 2. 为测试玩家初始化空背包
     * 3. 建立测试数据的基础状态
     */
    @BeforeEach
    public void setUp() {
        // 创建背包模块实例
        bagModule = new BagModule();
        // 为测试玩家初始化背包数据
        bagModule.initPlayerBag(TEST_PLAYER_ID);
    }
    
    /**
     * 测试添加物品功能
     * 
     * 功能说明：
     * - 验证向玩家背包添加物品的基本功能
     * - 检查添加后背包状态的正确性
     * - 确保物品数量和ID的准确记录
     * 
     * 测试步骤：
     * 1. 调用添加物品接口，添加指定物品
     * 2. 验证操作返回成功状态
     * 3. 查询背包信息，确认物品正确添加
     * 4. 验证物品ID和数量的准确性
     * 
     * 业务验证：
     * - 添加操作必须返回成功结果
     * - 背包中物品数量必须正确
     * - 物品ID必须与添加的物品匹配
     */
    @Test
    public void testAddItem() {
        // 执行添加物品操作
        Result<Void> result = bagModule.addItem(TEST_PLAYER_ID, 1001, 1);
        assertTrue(result.isSuccess(), "添加物品操作应该成功");
        
        // 验证背包状态
        Result<BagModule.BagInfo> bagInfo = bagModule.getBagInfo(TEST_PLAYER_ID);
        assertTrue(bagInfo.isSuccess(), "获取背包信息应该成功");
        assertEquals(1, bagInfo.getData().getItems().size(), "背包中应该有1个物品");
        assertEquals(1001, bagInfo.getData().getItems().get(0).getItemId(), "物品ID应该正确");
    }
    
    /**
     * 测试使用物品功能
     * 
     * 功能说明：
     * - 验证玩家使用背包中物品的功能
     * - 检查使用后物品数量的正确扣除
     * - 确保部分使用时的数量计算准确
     * 
     * 测试步骤：
     * 1. 先添加一定数量的物品到背包
     * 2. 获取物品的唯一标识符
     * 3. 执行使用物品操作，使用部分数量
     * 4. 验证剩余数量的准确性
     * 
     * 业务场景：
     * - 玩家使用消耗品（如药水、食物等）
     * - 物品数量的正确扣除和更新
     * - 背包状态的实时同步
     */
    @Test
    public void testUseItem() {
        // 准备测试数据：添加5个物品
        bagModule.addItem(TEST_PLAYER_ID, 1002, 5);
        
        // 获取物品的唯一标识
        Result<BagModule.BagInfo> bagInfo = bagModule.getBagInfo(TEST_PLAYER_ID);
        long itemUid = bagInfo.getData().getItems().get(0).getItemUid();
        
        // 使用2个物品
        Result<Void> result = bagModule.useItem(TEST_PLAYER_ID, itemUid, 2);
        assertTrue(result.isSuccess(), "使用物品操作应该成功");
        
        // 验证剩余数量：应该剩余3个
        bagInfo = bagModule.getBagInfo(TEST_PLAYER_ID);
        assertEquals(3, bagInfo.getData().getItems().get(0).getCount(), 
                "使用2个物品后应该剩余3个");
    }
    
    /**
     * 测试使用全部物品功能
     * 
     * 功能说明：
     * - 验证当物品全部使用完毕时的处理逻辑
     * - 检查物品耗尽后是否正确从背包中移除
     * - 确保背包清理机制的正确性
     * 
     * 测试步骤：
     * 1. 添加指定数量的物品到背包
     * 2. 使用全部物品数量
     * 3. 验证使用操作成功
     * 4. 确认物品已从背包中完全移除
     * 
     * 业务场景：
     * - 玩家用完所有同类消耗品
     * - 物品堆叠的自动清理
     * - 背包空间的释放和整理
     */
    @Test
    public void testUseAllItems() {
        // 准备测试数据：添加3个物品
        bagModule.addItem(TEST_PLAYER_ID, 1002, 3);
        
        // 获取物品的唯一标识
        Result<BagModule.BagInfo> bagInfo = bagModule.getBagInfo(TEST_PLAYER_ID);
        long itemUid = bagInfo.getData().getItems().get(0).getItemUid();
        
        // 使用全部3个物品
        Result<Void> result = bagModule.useItem(TEST_PLAYER_ID, itemUid, 3);
        assertTrue(result.isSuccess(), "使用全部物品操作应该成功");
        
        // 验证物品已被完全移除
        bagInfo = bagModule.getBagInfo(TEST_PLAYER_ID);
        assertEquals(0, bagInfo.getData().getItems().size(), 
                "使用完所有物品后背包应该为空");
    }
}