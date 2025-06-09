package com.game.service.logic.handler;

import com.game.common.game.Result;
import com.game.common.game.Task;
import com.game.service.logic.manager.ModuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 任务消息处理器
 * 处理任务相关的协议消息
 *
 * @author lx
 * @date 2025/06/08
 */
public class TaskHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskHandler.class);
    private final ModuleManager moduleManager;
    
    public TaskHandler(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
    
    /**
     * 处理获取任务列表请求
     */
    public Result<List<Task>> handleGetTaskList(long playerId) {
        try {
            logger.debug("Handle get task list: playerId={}", playerId);
            return moduleManager.getTaskModule().getTaskList(playerId);
        } catch (Exception e) {
            logger.error("Failed to handle get task list: playerId={}", playerId, e);
            return Result.failure("Failed to get task list: " + e.getMessage());
        }
    }
    
    /**
     * 处理接取任务请求
     */
    public Result<Task> handleAcceptTask(long playerId, int taskConfigId) {
        try {
            logger.debug("Handle accept task: playerId={}, taskConfigId={}", playerId, taskConfigId);
            return moduleManager.getTaskModule().acceptTask(playerId, taskConfigId);
        } catch (Exception e) {
            logger.error("Failed to handle accept task: playerId={}, taskConfigId={}", 
                    playerId, taskConfigId, e);
            return Result.failure("Failed to accept task: " + e.getMessage());
        }
    }
    
    /**
     * 处理完成任务请求
     */
    public Result<Void> handleCompleteTask(long playerId, long taskId) {
        try {
            logger.debug("Handle complete task: playerId={}, taskId={}", playerId, taskId);
            return moduleManager.getTaskModule().completeTask(playerId, taskId);
        } catch (Exception e) {
            logger.error("Failed to handle complete task: playerId={}, taskId={}", 
                    playerId, taskId, e);
            return Result.failure("Failed to complete task: " + e.getMessage());
        }
    }
}