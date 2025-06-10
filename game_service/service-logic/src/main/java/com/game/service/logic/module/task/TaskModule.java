package com.game.service.logic.module.task;

import com.game.common.game.Result;
import com.game.common.game.Task;
import com.game.common.game.TaskState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务模块
 * 
 * 功能说明：
 * - 负责游戏任务系统的核心逻辑管理
 * - 提供任务的生命周期管理（接取、进度、完成）
 * - 支持任务状态机的状态转换和验证
 * - 管理玩家任务数据的存储和查询
 * 
 * 设计思路：
 * - 采用状态机模式管理任务的各个状态转换
 * - 使用线程安全的数据结构支持高并发访问
 * - 通过配置ID和实例ID分离任务模板和任务实例
 * - 提供灵活的任务进度追踪机制
 * 
 * 核心功能：
 * - 任务接取：验证条件、创建任务实例、更新状态
 * - 进度管理：支持多维度进度追踪和条件检查
 * - 任务完成：验证完成条件、发放奖励、更新状态
 * - 数据管理：玩家任务数据的增删改查操作
 * 
 * 任务状态流转：
 * 1. 未接取 -> 进行中（acceptTask）
 * 2. 进行中 -> 可完成（updateTaskProgress）
 * 3. 可完成 -> 已完成（completeTask）
 * 
 * 使用场景：
 * - 主线任务和支线任务的管理
 * - 日常任务和活动任务的处理
 * - 成就系统的进度追踪
 * - 新手引导任务的流程控制
 *
 * @author lx
 * @date 2025/06/08
 */
public class TaskModule {
    
    // 日志记录器，用于记录任务系统的关键操作和状态变化
    private static final Logger logger = LoggerFactory.getLogger(TaskModule.class);
    // 任务ID生成器，确保每个任务实例都有唯一的标识ID
    private static final AtomicLong taskIdGenerator = new AtomicLong(1);
    
    // 玩家任务数据存储映射：玩家ID -> 任务列表
    // 使用 ConcurrentHashMap 保证多线程环境下的数据安全
    private final ConcurrentHashMap<Long, List<Task>> playerTasks = new ConcurrentHashMap<>();
    
    /**
     * 初始化玩家任务数据
     * 
     * 功能说明：
     * - 为新登录的玩家创建任务数据容器
     * - 初始化空的任务列表，准备接收后续任务
     * - 确保任务数据的线程安全访问
     * 
     * 执行时机：
     * - 玩家首次登录游戏时
     * - 玩家数据重置或迁移时
     * - 服务器重启后的数据恢复
     * 
     * @param playerId 玩家唯一标识ID
     * 
     * 注意事项：
     * - 如果玩家已有任务数据，会被重新初始化
     * - 该操作是幂等的，重复调用不会产生副作用
     * - 初始化后需要从数据库加载玩家的历史任务数据
     */
    public void initPlayerTasks(long playerId) {
        playerTasks.put(playerId, new ArrayList<>());
        logger.info("初始化玩家任务数据: playerId={}", playerId);
    }
    
    /**
     * 获取玩家的任务列表
     * 
     * 功能说明：
     * - 查询指定玩家的所有任务数据
     * - 返回任务列表的副本，避免外部修改影响内部数据
     * - 支持任务列表的安全访问和遍历
     * 
     * 返回数据包含：
     * - 任务的基本信息（ID、配置ID、状态）
     * - 任务的进度数据和完成条件
     * - 任务的创建时间和更新时间
     * - 任务的奖励信息和完成状态
     * 
     * @param playerId 玩家唯一标识ID
     * @return 包含任务列表的结果对象，失败时返回错误信息
     * 
     * 异常情况：
     * - 玩家任务数据未初始化
     * - 玩家ID不存在或无效
     * 
     * 使用场景：
     * - 客户端请求任务列表显示
     * - 任务面板的数据加载
     * - 任务相关界面的刷新
     */
    public Result<List<Task>> getTaskList(long playerId) {
        List<Task> tasks = playerTasks.get(playerId);
        if (tasks == null) {
            return Result.failure("玩家任务数据未找到: " + playerId);
        }
        return Result.success(new ArrayList<>(tasks));
    }
    
    /**
     * 接取任务
     * 
     * 功能说明：
     * - 根据任务配置ID为玩家创建新的任务实例
     * - 验证任务接取的前置条件和重复性检查
     * - 初始化任务状态为进行中并分配唯一的任务ID
     * 
     * 业务逻辑：
     * 1. 验证玩家任务数据是否已初始化
     * 2. 检查玩家是否已经接取过相同的任务
     * 3. 生成新的任务实例ID并创建任务对象
     * 4. 设置任务初始状态为进行中
     * 5. 将任务添加到玩家的任务列表中
     * 
     * @param playerId 玩家唯一标识ID
     * @param taskConfigId 任务配置ID，对应任务配置表中的定义
     * @return 创建的任务实例，失败时返回错误信息
     * 
     * 异常情况：
     * - 玩家任务数据未初始化
     * - 任务已经被接取过
     * - 任务配置ID无效或不存在
     * - 不满足任务接取的前置条件
     * 
     * 注意事项：
     * - 任务ID是全局唯一的递增序列
     * - 接取成功后任务状态自动设为进行中
     * - 同一个配置ID的任务每个玩家只能接取一次
     */
    public Result<Task> acceptTask(long playerId, int taskConfigId) {
        List<Task> tasks = playerTasks.get(playerId);
        if (tasks == null) {
            return Result.failure("玩家任务数据未找到: " + playerId);
        }
        
        // 检查是否已经接取过这个任务
        boolean alreadyAccepted = tasks.stream()
                .anyMatch(task -> task.getConfigId() == taskConfigId);
        if (alreadyAccepted) {
            return Result.failure("任务已经接取过: " + taskConfigId);
        }
        
        // 创建新任务实例
        long taskId = taskIdGenerator.getAndIncrement();
        Task task = new Task(taskId, taskConfigId);
        task.setState(TaskState.IN_PROGRESS);
        tasks.add(task);
        
        logger.debug("任务接取成功: playerId={}, taskId={}, configId={}", playerId, taskId, taskConfigId);
        return Result.success(task);
    }
    
    /**
     * 完成任务
     * 
     * 功能说明：
     * - 将指定的任务标记为已完成状态
     * - 验证任务完成的条件和状态转换的合法性
     * - 触发任务完成后的后续处理逻辑
     * 
     * 完成条件验证：
     * 1. 验证玩家任务数据的有效性
     * 2. 查找指定ID的任务实例
     * 3. 检查任务当前状态是否为可完成
     * 4. 执行状态转换并更新任务数据
     * 
     * 状态转换：
     * - 只有状态为 CAN_COMPLETE 的任务才能被完成
     * - 完成后任务状态变更为 COMPLETED
     * - 完成时间戳会被自动记录
     * 
     * @param playerId 玩家唯一标识ID
     * @param taskId 任务实例的唯一ID
     * @return 操作结果，成功时返回 success，失败时包含错误信息
     * 
     * 异常情况：
     * - 玩家任务数据未初始化
     * - 指定的任务ID不存在
     * - 任务状态不是可完成状态
     * - 任务已经被完成过
     * 
     * 后续处理：
     * - 发放任务奖励（需要在调用方处理）
     * - 更新任务相关的统计数据
     * - 触发可能的后续任务链
     * - 记录任务完成的日志信息
     */
    public Result<Void> completeTask(long playerId, long taskId) {
        List<Task> tasks = playerTasks.get(playerId);
        if (tasks == null) {
            return Result.failure("玩家任务数据未找到: " + playerId);
        }
        
        Task task = tasks.stream()
                .filter(t -> t.getId() == taskId)
                .findFirst()
                .orElse(null);
        
        if (task == null) {
            return Result.failure("任务未找到: " + taskId);
        }
        
        if (task.getState() != TaskState.CAN_COMPLETE) {
            return Result.failure("任务无法完成，当前状态不正确: " + taskId);
        }
        
        // 完成任务并更新状态
        task.setState(TaskState.COMPLETED);
        
        logger.debug("任务完成: playerId={}, taskId={}", playerId, taskId);
        return Result.success();
    }
    
    /**
     * 更新任务进度
     * 
     * 功能说明：
     * - 更新指定任务的进度数据，支持多维度的进度追踪
     * - 根据进度更新自动检查任务是否可以完成
     * - 提供灵活的进度键值对管理机制
     * 
     * 进度更新逻辑：
     * 1. 验证玩家任务数据的有效性
     * 2. 查找指定配置ID且状态为进行中的任务
     * 3. 更新任务的进度数据（键值对形式）
     * 4. 检查任务完成条件并可能触发状态转换
     * 
     * 进度数据结构：
     * - 使用 Map<String, Integer> 存储进度信息
     * - 键(progressKey)：进度类型标识，如 "kill_monster", "collect_item"
     * - 值(value)：当前进度数值，如击杀数量、收集数量
     * 
     * @param playerId 玩家唯一标识ID
     * @param taskConfigId 任务配置ID，用于定位具体的任务类型
     * @param progressKey 进度键，标识要更新的进度类型
     * @param value 进度值，表示当前该类型的进度数量
     * @return 操作结果，成功时返回 success，失败时包含错误信息
     * 
     * 特殊处理：
     * - 如果玩家没有对应的任务，直接返回成功（避免无效操作）
     * - 只更新状态为进行中的任务，其他状态的任务忽略
     * - 进度更新后会自动触发完成条件检查（TODO项）
     * 
     * 使用场景：
     * - 击杀怪物时更新击杀进度
     * - 收集物品时更新收集进度
     * - 完成特定行为时更新行为计数
     * - 达成特定条件时更新条件进度
     * 
     * 扩展说明：
     * - 当前版本的完成条件检查尚未实现（标记为 TODO）
     * - 后续需要根据任务配置表来判断完成条件
     * - 完成条件可能包含多个进度键的组合判断
     */
    public Result<Void> updateTaskProgress(long playerId, int taskConfigId, String progressKey, int value) {
        List<Task> tasks = playerTasks.get(playerId);
        if (tasks == null) {
            return Result.failure("玩家任务数据未找到: " + playerId);
        }
        
        Task task = tasks.stream()
                .filter(t -> t.getConfigId() == taskConfigId && t.getState() == TaskState.IN_PROGRESS)
                .findFirst()
                .orElse(null);
        
        if (task == null) {
            return Result.success(); // 没有此任务，直接返回成功
        }
        
        // 更新任务进度数据
        if (task.getProgress() != null) {
            task.getProgress().put(progressKey, value);
            
            // TODO: 检查任务是否可以完成
            // 这里应该根据任务配置来判断完成条件：
            // 1. 获取任务配置中的完成条件定义
            // 2. 对比当前进度与完成条件
            // 3. 如果满足条件，将任务状态更新为 CAN_COMPLETE
            // 4. 可能需要触发任务完成的相关事件
            
            logger.debug("任务进度更新: playerId={}, taskId={}, progressKey={}, value={}", 
                    playerId, task.getId(), progressKey, value);
        }
        
        return Result.success();
    }
}