package com.game.frame.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 游戏时钟
 * 提供游戏帧率控制和时间管理
 *
 * @author lx
 * @date 2025/06/08
 */
public class GameClock {
    
    private static final Logger logger = LoggerFactory.getLogger(GameClock.class);
    
    // 逻辑帧率（30fps）
    private static final int FRAME_RATE = 30;
    private static final long FRAME_DURATION_MS = 1000 / FRAME_RATE;
    
    private static volatile long currentFrame = 0;
    private static volatile long gameStartTime = System.currentTimeMillis();
    
    /**
     * 获取当前帧
     * 
     * @return 当前帧数
     */
    public static long getCurrentFrame() {
        return currentFrame;
    }
    
    /**
     * 获取游戏时间（毫秒）
     * 
     * @return 游戏运行时间
     */
    public static long getGameTime() {
        return System.currentTimeMillis() - gameStartTime;
    }
    
    /**
     * 获取帧率
     * 
     * @return 帧率
     */
    public static int getFrameRate() {
        return FRAME_RATE;
    }
    
    /**
     * 获取帧持续时间
     * 
     * @return 帧持续时间（毫秒）
     */
    public static long getFrameDuration() {
        return FRAME_DURATION_MS;
    }
    
    /**
     * 更新帧计数（由FrameScheduler调用）
     */
    static void updateFrame() {
        currentFrame++;
    }
    
    /**
     * 重置游戏时钟
     */
    public static void reset() {
        currentFrame = 0;
        gameStartTime = System.currentTimeMillis();
        logger.info("Game clock reset");
    }
}