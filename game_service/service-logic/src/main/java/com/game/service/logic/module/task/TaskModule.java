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
 * 包含任务类型定义、任务状态机、任务触发器
 *
 * @author lx
 * @date 2025/06/08
 */
public class TaskModule {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskModule.class);
    private static final AtomicLong taskIdGenerator = new AtomicLong(1);
    
    // 玩家任务数据 playerId -> List<Task>
    private final ConcurrentHashMap<Long, List<Task>> playerTasks = new ConcurrentHashMap<>();
    
    /**
     * 初始化玩家任务
     */
    public void initPlayerTasks(long playerId) {
        playerTasks.put(playerId, new ArrayList<>());
        logger.info("Initialized tasks for player: {}", playerId);
    }
    
    /**
     * 获取玩家任务列表
     */
    public Result<List<Task>> getTaskList(long playerId) {
        List<Task> tasks = playerTasks.get(playerId);
        if (tasks == null) {
            return Result.failure("Player tasks not found: " + playerId);
        }
        return Result.success(new ArrayList<>(tasks));
    }
    
    /**
     * 接取任务
     */
    public Result<Task> acceptTask(long playerId, int taskConfigId) {
        List<Task> tasks = playerTasks.get(playerId);
        if (tasks == null) {
            return Result.failure("Player tasks not found: " + playerId);
        }
        
        // 检查是否已经接取过这个任务
        boolean alreadyAccepted = tasks.stream()
                .anyMatch(task -> task.getConfigId() == taskConfigId);
        if (alreadyAccepted) {
            return Result.failure("Task already accepted: " + taskConfigId);
        }
        
        // 创建新任务
        long taskId = taskIdGenerator.getAndIncrement();
        Task task = new Task(taskId, taskConfigId);
        task.setState(TaskState.IN_PROGRESS);
        tasks.add(task);
        
        logger.debug("Accepted task: playerId={}, taskId={}, configId={}", playerId, taskId, taskConfigId);
        return Result.success(task);
    }
    
    /**
     * 完成任务
     */
    public Result<Void> completeTask(long playerId, long taskId) {
        List<Task> tasks = playerTasks.get(playerId);
        if (tasks == null) {
            return Result.failure("Player tasks not found: " + playerId);
        }
        
        Task task = tasks.stream()
                .filter(t -> t.getId() == taskId)
                .findFirst()
                .orElse(null);
        
        if (task == null) {
            return Result.failure("Task not found: " + taskId);
        }
        
        if (task.getState() != TaskState.CAN_COMPLETE) {
            return Result.failure("Task cannot be completed: " + taskId);
        }
        
        // 完成任务
        task.setState(TaskState.COMPLETED);
        
        logger.debug("Completed task: playerId={}, taskId={}", playerId, taskId);
        return Result.success();
    }
    
    /**
     * 更新任务进度
     */
    public Result<Void> updateTaskProgress(long playerId, int taskConfigId, String progressKey, int value) {
        List<Task> tasks = playerTasks.get(playerId);
        if (tasks == null) {
            return Result.failure("Player tasks not found: " + playerId);
        }
        
        Task task = tasks.stream()
                .filter(t -> t.getConfigId() == taskConfigId && t.getState() == TaskState.IN_PROGRESS)
                .findFirst()
                .orElse(null);
        
        if (task == null) {
            return Result.success(); // 没有此任务，直接返回成功
        }
        
        // 更新进度
        if (task.getProgress() != null) {
            task.getProgress().put(progressKey, value);
            
            // TODO: 检查任务是否可以完成
            // 这里应该根据任务配置来判断
            logger.debug("Updated task progress: playerId={}, taskId={}, key={}, value={}", 
                    playerId, task.getId(), progressKey, value);
        }
        
        return Result.success();
    }
}