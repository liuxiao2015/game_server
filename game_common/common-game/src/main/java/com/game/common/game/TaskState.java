package com.game.common.game;

/**
 * 任务状态枚举
 * 
 * 功能说明：
 * - 定义游戏任务系统中所有可能的任务状态
 * - 提供任务状态的代码映射和描述信息
 * - 支持任务状态的转换和查询操作
 * - 作为任务系统状态管理的核心枚举类
 * 
 * 设计思路：
 * - 使用枚举类型确保任务状态的类型安全
 * - 为每个状态分配唯一的数字代码便于存储和传输
 * - 提供中文描述便于界面显示和调试
 * - 支持通过代码反向查找状态枚举
 * 
 * 状态流转：
 * 1. NOT_ACCEPTED(未接取) → IN_PROGRESS(进行中)：玩家接取任务
 * 2. IN_PROGRESS(进行中) → CAN_COMPLETE(可完成)：完成任务条件
 * 3. CAN_COMPLETE(可完成) → COMPLETED(已完成)：提交任务奖励
 * 
 * 业务规则：
 * - 任务状态通常按顺序流转，不可逆转
 * - 某些特殊任务可能支持重置或重新接取
 * - 已完成的任务可能影响后续任务的解锁
 * 
 * 使用场景：
 * - 任务系统的状态管理和流转控制
 * - 客户端任务界面的状态显示
 * - 任务完成度统计和进度跟踪
 * - 数据库任务数据的存储和查询
 *
 * @author lx
 * @date 2025/06/08
 */
public enum TaskState {
    
    /** 任务未接取状态 - 玩家尚未开始该任务，可以选择接取 */
    NOT_ACCEPTED(0, "未接取"),
    
    /** 任务进行中状态 - 玩家已接取任务，正在完成任务目标 */
    IN_PROGRESS(1, "进行中"),
    
    /** 任务可完成状态 - 任务目标已达成，可以提交获得奖励 */
    CAN_COMPLETE(2, "可完成"),
    
    /** 任务已完成状态 - 任务已提交，奖励已发放，任务结束 */
    COMPLETED(3, "已完成");
    
    // 任务状态的数字代码，用于数据库存储和网络传输
    // 每个状态都有唯一的代码，便于系统识别和处理
    private final int code;
    
    // 任务状态的中文描述，用于界面显示和日志记录
    // 提供用户友好的状态说明，便于理解和调试
    private final String description;
    
    /**
     * 任务状态枚举构造函数
     * 
     * @param code 状态代码，用于数据存储和传输
     * @param description 状态描述，用于界面显示
     */
    TaskState(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取任务状态的数字代码
     * 
     * @return 状态代码，用于数据库存储和网络协议传输
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取任务状态的中文描述
     * 
     * @return 状态描述字符串，用于界面显示和用户提示
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据状态代码获取对应的任务状态枚举
     * 
     * 功能说明：
     * - 将数字代码转换为对应的任务状态枚举实例
     * - 主要用于数据库查询结果和网络协议的反序列化
     * - 提供类型安全的状态代码解析功能
     * 
     * 查找逻辑：
     * 1. 遍历所有任务状态枚举值
     * 2. 比较输入代码与枚举的代码值
     * 3. 找到匹配的枚举则返回对应实例
     * 4. 未找到匹配项则抛出异常
     * 
     * @param code 要查找的状态代码
     * @return 对应的任务状态枚举实例
     * @throws IllegalArgumentException 当代码不存在时抛出异常
     * 
     * 使用场景：
     * - 数据库查询结果的状态转换
     * - 网络协议消息的状态解析
     * - 配置文件中状态码的解析
     * 
     * 异常处理：
     * - 输入无效代码时抛出详细的错误信息
     * - 便于调试和问题定位
     */
    public static TaskState fromCode(int code) {
        // 遍历所有枚举值查找匹配的代码
        for (TaskState state : values()) {
            if (state.code == code) {
                return state;
            }
        }
        // 未找到匹配的代码，抛出异常并提供详细信息
        throw new IllegalArgumentException("Unknown task state code: " + code);
    }
}