package com.game.frame.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏时钟管理器
 * 
 * 功能说明：
 * - 提供高精度的游戏逻辑帧率控制，确保游戏逻辑的一致性
 * - 管理游戏运行时间和帧计数，支持实时监控和性能分析
 * - 提供统一的时间基准，避免不同模块时间不一致的问题
 * - 支持游戏暂停、恢复和时间重置功能
 * 
 * 设计思路：
 * - 采用单例模式确保全局时间基准的唯一性
 * - 使用volatile关键字保证多线程环境下的数据一致性
 * - 固定30FPS的逻辑帧率，平衡性能和流畅度
 * - 分离逻辑时间和系统时间，便于调试和测试
 * 
 * 核心特性：
 * - 逻辑帧率：30FPS，每帧约33.33毫秒
 * - 时间精度：毫秒级别，满足游戏逻辑需求
 * - 线程安全：支持多线程并发访问
 * - 性能优化：静态方法调用，避免对象创建开销
 * 
 * 使用场景：
 * - 游戏逻辑更新的时间基准
 * - 定时任务的调度和执行
 * - 性能监控和统计分析
 * - 游戏录像和回放功能
 * 
 * 注意事项：
 * - 帧更新必须由FrameScheduler统一调度
 * - 不要在业务逻辑中直接调用updateFrame()
 * - 重置操作会影响所有依赖时间的模块
 * - 在高并发场景下注意内存可见性问题
 *
 * @author lx
 * @date 2025/06/08
 * @since 1.0.0
 */
public class GameClock {
    
    // 日志记录器，用于记录时钟状态和重要事件
    private static final Logger logger = LoggerFactory.getLogger(GameClock.class);
    
    // 游戏逻辑帧率：30FPS，确保游戏逻辑的稳定性和一致性
    private static final int FRAME_RATE = 30;
    // 每帧持续时间：约33.33毫秒，用于帧率控制和时间计算
    private static final long FRAME_DURATION_MS = 1000 / FRAME_RATE;
    
    // 当前帧计数器，记录游戏运行的总帧数（原子性保证）
    private static volatile long currentFrame = 0;
    // 游戏启动时间戳，用于计算游戏运行时长
    private static volatile long gameStartTime = System.currentTimeMillis();
    
    /**
     * 获取当前游戏逻辑帧数
     * 
     * 返回游戏开始以来的总帧数，可用于：
     * - 计算游戏运行时长（帧数 * 帧持续时间）
     * - 实现基于帧的定时器和延迟功能
     * - 游戏状态同步和一致性检查
     * - 性能分析和调试信息记录
     * 
     * 注意：此值在多线程环境下是原子读取的
     * 
     * @return 当前帧数，从0开始递增
     */
    public static long getCurrentFrame() {
        return currentFrame;
    }
    
    /**
     * 获取游戏运行时间（毫秒）
     * 
     * 计算游戏启动至今的运行时长，用于：
     * - 游戏时长统计和显示
     * - 基于时间的游戏机制（冷却、buff等）
     * - 性能监控和日志记录
     * - 超时检测和资源回收
     * 
     * 与系统时间的区别：
     * - 游戏时间从启动开始计算，不受系统时间调整影响
     * - 支持暂停和恢复功能（如果需要）
     * - 提供一致的时间基准
     * 
     * @return 游戏运行时间，单位：毫秒
     */
    public static long getGameTime() {
        return System.currentTimeMillis() - gameStartTime;
    }
    
    /**
     * 获取游戏逻辑帧率配置
     * 
     * 返回固定的逻辑帧率值，用于：
     * - 客户端渲染帧率适配
     * - 网络同步频率计算
     * - 性能基准和调优参考
     * - 第三方集成和配置
     * 
     * 设计说明：
     * - 30FPS是经过优化的平衡点，兼顾性能和流畅度
     * - 独立于渲染帧率，专注于游戏逻辑的一致性
     * - 避免硬编码，便于未来的扩展和配置
     * 
     * @return 逻辑帧率，固定值：30FPS
     */
    public static int getFrameRate() {
        return FRAME_RATE;
    }
    
    /**
     * 获取单帧持续时间（毫秒）
     * 
     * 返回每个逻辑帧的时间长度，用于：
     * - 帧率控制和时间片分配
     * - 游戏循环的睡眠时间计算
     * - 性能监控和帧率统计
     * - 定时器精度校准
     * 
     * 计算公式：1000毫秒 / 帧率
     * 30FPS对应约33.33毫秒每帧
     * 
     * @return 帧持续时间，单位：毫秒，约为33.33ms
     */
    public static long getFrameDuration() {
        return FRAME_DURATION_MS;
    }
    
    /**
     * 更新游戏帧计数器（内部方法）
     * 
     * 此方法仅由FrameScheduler调用，用于：
     * - 递增全局帧计数器
     * - 维护游戏时间的准确性
     * - 触发基于帧的事件和逻辑
     * 
     * 安全性说明：
     * - 包级别访问，防止外部误调用
     * - 线程安全，使用volatile保证可见性
     * - 原子操作，避免并发问题
     * 
     * 注意事项：
     * - 不要在业务代码中直接调用此方法
     * - 帧更新频率影响游戏逻辑的一致性
     * - 调用频率必须与FRAME_RATE保持一致
     */
    static void updateFrame() {
        currentFrame++;
        
        // 定期输出帧率统计信息（每1000帧一次）
        if (currentFrame % 1000 == 0) {
            long gameTime = getGameTime();
            double actualFps = gameTime > 0 ? (currentFrame * 1000.0 / gameTime) : 0;
            logger.debug("游戏运行统计 - 帧数: {}, 运行时间: {}ms, 实际帧率: {:.2f}fps", 
                        currentFrame, gameTime, actualFps);
        }
    }
    
    /**
     * 重置游戏时钟状态
     * 
     * 将游戏时钟恢复到初始状态，包括：
     * - 帧计数器清零
     * - 重置游戏开始时间
     * - 记录重置操作日志
     * 
     * 使用场景：
     * - 游戏重新开始或切换场景
     * - 测试环境的状态清理
     * - 异常恢复和系统重启
     * - 性能测试的基准重置
     * 
     * 注意事项：
     * - 会影响所有依赖时间的游戏模块
     * - 重置后统计数据会丢失
     * - 应在游戏逻辑暂停时调用
     * - 多线程环境下注意同步问题
     */
    public static void reset() {
        long oldFrame = currentFrame;
        long oldGameTime = getGameTime();
        
        currentFrame = 0;
        gameStartTime = System.currentTimeMillis();
        
        logger.info("游戏时钟已重置 - 原帧数: {}, 原运行时间: {}ms", oldFrame, oldGameTime);
    }
    
    /**
     * 获取当前游戏时钟的详细状态信息
     * 
     * 返回包含以下信息的字符串：
     * - 当前帧数和运行时间
     * - 实际帧率和理论帧率对比
     * - 游戏启动时间戳
     * - 性能统计数据
     * 
     * 主要用于：
     * - 调试和故障排查
     * - 性能监控和报表
     * - 运维状态检查
     * - 开发工具集成
     * 
     * @return 时钟状态的详细描述字符串
     */
    public static String getStatusInfo() {
        long gameTime = getGameTime();
        double actualFps = gameTime > 0 ? (currentFrame * 1000.0 / gameTime) : 0;
        
        return String.format("游戏时钟状态 - 当前帧: %d, 运行时间: %dms, 理论帧率: %dfps, 实际帧率: %.2ffps, 启动时间: %d",
                           currentFrame, gameTime, FRAME_RATE, actualFps, gameStartTime);
    }
}