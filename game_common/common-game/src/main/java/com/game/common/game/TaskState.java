package com.game.common.game;

/**
 * 任务状态枚举
 *
 * @author lx
 * @date 2025/06/08
 */
public enum TaskState {
    
    NOT_ACCEPTED(0, "未接取"),
    IN_PROGRESS(1, "进行中"),
    CAN_COMPLETE(2, "可完成"),
    COMPLETED(3, "已完成");
    
    private final int code;
    private final String description;
    
    TaskState(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TaskState fromCode(int code) {
        for (TaskState state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown task state code: " + code);
    }
}