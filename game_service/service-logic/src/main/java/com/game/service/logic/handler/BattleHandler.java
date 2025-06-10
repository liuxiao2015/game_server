package com.game.service.logic.handler;

import com.game.common.game.BattleResult;
import com.game.common.game.Result;
import com.game.service.logic.manager.ModuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 战斗消息处理器
 * 
 * 功能说明：
 * - 处理客户端发送的战斗相关协议消息
 * - 提供战斗系统的网络接口和消息路由
 * - 封装战斗业务逻辑并提供统一的错误处理
 * - 集成模块管理器实现战斗功能的调用
 * 
 * 设计思路：
 * - 采用处理器模式，专门处理战斗相关的消息
 * - 通过模块管理器访问战斗模块，保持松耦合
 * - 提供统一的异常处理和错误信息返回
 * - 支持详细的日志记录便于问题追踪
 * 
 * 核心功能：
 * - 战斗发起：处理玩家发起战斗的请求
 * - 战斗结果：返回战斗过程和结果数据
 * - 奖励处理：处理战斗胜利的奖励发放
 * - 状态更新：更新玩家的战斗相关状态
 * 
 * 使用场景：
 * - PVE战斗：玩家与怪物的战斗
 * - PVP战斗：玩家间的对战
 * - 副本战斗：特殊副本中的战斗
 * - 竞技场战斗：排名系统中的对战
 *
 * @author lx
 * @date 2025/06/08
 */
public class BattleHandler {
    
    // 日志记录器，用于记录战斗消息处理的关键信息和调试数据
    private static final Logger logger = LoggerFactory.getLogger(BattleHandler.class);
    // 模块管理器，用于访问战斗模块和其他相关业务模块
    private final ModuleManager moduleManager;
    
    /**
     * 构造战斗消息处理器
     * 
     * @param moduleManager 模块管理器实例，用于访问战斗相关的业务服务
     */
    public BattleHandler(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
    
    /**
     * 处理战斗开始请求
     * 
     * 功能说明：
     * - 处理客户端发起战斗的请求消息
     * - 验证战斗参数和玩家状态
     * - 执行战斗逻辑并返回战斗结果
     * 
     * 处理流程：
     * 1. 记录战斗请求的详细参数
     * 2. 调用战斗模块执行战斗逻辑
     * 3. 处理战斗过程中的各种异常
     * 4. 返回战斗结果和奖励信息
     * 
     * @param playerId 参与战斗的玩家ID
     * @param monsterId 战斗目标怪物的ID
     * @return 包含战斗结果的对象，包括胜负、经验值、掉落物品等
     * 
     * 异常处理：
     * - 玩家状态异常无法战斗
     * - 怪物配置不存在或无效
     * - 战斗过程中的系统异常
     */
    public Result<BattleResult> handleBattleStart(long playerId, int monsterId) {
        try {
            logger.debug("处理战斗开始请求: playerId={}, monsterId={}", playerId, monsterId);
            return moduleManager.getBattleModule().startBattle(playerId, monsterId);
        } catch (Exception e) {
            logger.error("战斗开始失败: playerId={}, monsterId={}", 
                    playerId, monsterId, e);
            return Result.failure("战斗开始失败: " + e.getMessage());
        }
    }
}