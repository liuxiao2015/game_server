package com.game.common.game;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务实体
 *
 * @author lx
 * @date 2025/06/08
 */
public class Task {
    
    private long id;
    private int configId;
    private TaskState state;
    private Map<String, Integer> progress;
    private LocalDateTime acceptTime;
    private LocalDateTime completeTime;
    
    /**

    
     * Task方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
    public Task() {}
    
    /**

    
     * Task方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
    public Task(long id, int configId) {
        this.id = id;
        this.configId = configId;
        this.state = TaskState.NOT_ACCEPTED;
        this.acceptTime = LocalDateTime.now();
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public int getConfigId() {
        return configId;
    }
    
    public void setConfigId(int configId) {
        this.configId = configId;
    }
    
    public TaskState getState() {
        return state;
    }
    
    public void setState(TaskState state) {
        this.state = state;
    }
    
    public Map<String, Integer> getProgress() {
        return progress;
    }
    
    public void setProgress(Map<String, Integer> progress) {
        this.progress = progress;
    }
    
    public LocalDateTime getAcceptTime() {
        return acceptTime;
    }
    
    public void setAcceptTime(LocalDateTime acceptTime) {
        this.acceptTime = acceptTime;
    }
    
    public LocalDateTime getCompleteTime() {
        return completeTime;
    }
    
    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }
}