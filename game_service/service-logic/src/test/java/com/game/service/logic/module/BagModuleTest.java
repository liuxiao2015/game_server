package com.game.service.logic.module;

import com.game.common.game.Result;
import com.game.service.logic.module.bag.BagModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 背包模块测试
 *
 * @author lx
 * @date 2025/06/08
 */
public class BagModuleTest {
    
    private BagModule bagModule;
    private static final long TEST_PLAYER_ID = 123L;
    
    @BeforeEach
    public void setUp() {
        bagModule = new BagModule();
        bagModule.initPlayerBag(TEST_PLAYER_ID);
    }
    
    @Test
    public void testAddItem() {
        Result<Void> result = bagModule.addItem(TEST_PLAYER_ID, 1001, 1);
        assertTrue(result.isSuccess());
        
        // 检查背包信息
        Result<BagModule.BagInfo> bagInfo = bagModule.getBagInfo(TEST_PLAYER_ID);
        assertTrue(bagInfo.isSuccess());
        assertEquals(1, bagInfo.getData().getItems().size());
        assertEquals(1001, bagInfo.getData().getItems().get(0).getItemId());
    }
    
    @Test
    public void testUseItem() {
        // 先添加物品
        bagModule.addItem(TEST_PLAYER_ID, 1002, 5);
        
        Result<BagModule.BagInfo> bagInfo = bagModule.getBagInfo(TEST_PLAYER_ID);
        long itemUid = bagInfo.getData().getItems().get(0).getItemUid();
        
        // 使用物品
        Result<Void> result = bagModule.useItem(TEST_PLAYER_ID, itemUid, 2);
        assertTrue(result.isSuccess());
        
        // 检查剩余数量
        bagInfo = bagModule.getBagInfo(TEST_PLAYER_ID);
        assertEquals(3, bagInfo.getData().getItems().get(0).getCount());
    }
    
    @Test
    public void testUseAllItems() {
        // 先添加物品
        bagModule.addItem(TEST_PLAYER_ID, 1002, 3);
        
        Result<BagModule.BagInfo> bagInfo = bagModule.getBagInfo(TEST_PLAYER_ID);
        long itemUid = bagInfo.getData().getItems().get(0).getItemUid();
        
        // 使用所有物品
        Result<Void> result = bagModule.useItem(TEST_PLAYER_ID, itemUid, 3);
        assertTrue(result.isSuccess());
        
        // 检查物品是否被移除
        bagInfo = bagModule.getBagInfo(TEST_PLAYER_ID);
        assertEquals(0, bagInfo.getData().getItems().size());
    }
}