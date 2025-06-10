package com.game.common.game;

/**
 * 任务状态枚举
 * 
 * 功能说明：
 * - 定义任务系统中所有可能的任务状态
 * - 提供状态码和描述的标准化管理
 * - 支持状态之间的转换验证和查询
 * - 用于任务状态机的状态流转控制
 * 
 * 设计思路：
 * - 采用枚举类型确保状态值的类型安全
 * - 每个状态包含数字码和中文描述便于存储和显示
 * - 提供状态码到枚举的转换方法支持数据序列化
 * - 状态定义遵循任务的自然生命周期流程
 * 
 * 状态流转：
 * 1. NOT_ACCEPTED(未接取) -> IN_PROGRESS(进行中)：玩家接取任务
 * 2. IN_PROGRESS(进行中) -> CAN_COMPLETE(可完成)：完成任务目标
 * 3. CAN_COMPLETE(可完成) -> COMPLETED(已完成)：提交任务获得奖励
 * 
 * 业务规则：
 * - 状态转换必须按照定义的流程进行，不允许跳跃
 * - 已完成的任务不能再次执行或重置状态
 * - 可完成状态的任务必须手动提交才能获得奖励
 * 
 * 使用场景：
 * - 任务数据的持久化存储
 * - 客户端任务界面的状态显示
 * - 任务系统的业务逻辑判断
 * - 任务状态变更的事件处理
 *
 * @author lx
 * @date 2025/06/08
 */
public enum TaskState {
    
    // 未接取状态：任务存在于配置中但玩家尚未接取
    NOT_ACCEPTED(0, "未接取"),
    // 进行中状态：玩家已接取任务，正在完成任务目标
    IN_PROGRESS(1, "进行中"),
    // 可完成状态：任务目标已达成，等待玩家提交获得奖励
    CAN_COMPLETE(2, "可完成"),
    // 已完成状态：任务已提交并获得奖励，任务流程结束
    COMPLETED(3, "已完成");
    
    // 状态对应的数字编码，用于数据库存储和网络传输
    private final int code;
    // 状态的中文描述，用于界面显示和日志记录
    private final String description;
    
    /**
     * 构造任务状态枚举
     * 
     * @param code 状态编码，唯一标识状态的数字值
     * @param description 状态描述，用于显示的中文说明
     */
    TaskState(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取状态编码
     * 
     * @return 状态对应的数字编码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取状态描述
     * 
     * @return 状态的中文描述文本
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据状态编码获取对应的枚举值
     * 
     * 功能说明：
     * - 将数字状态码转换为对应的枚举对象
     * - 用于数据库查询结果和网络消息的反序列化
     * - 提供类型安全的状态值获取方式
     * 
     * @param code 状态编码
     * @return 对应的TaskState枚举值
     * @throws IllegalArgumentException 当状态码无效时抛出异常
     * 
     * 使用场景：
     * - 数据库查询结果的状态转换
     * - 网络消息解析中的状态获取
     * - 配置文件加载时的状态验证
     */
    public static TaskState fromCode(int code) {
        for (TaskState state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        throw new IllegalArgumentException("未知的任务状态编码: " + code);
    }
}