package com.game.frame.netty.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Redis-based session storage interface implementation
 * 
 * 功能说明：
 * - 定义Redis会话存储的接口规范，具体实现由业务层提供
 * - 提供高性能的会话数据读写操作，支持会话的持久化和过期管理
 * - 实现读写分离优化，提升并发访问性能
 * - 支持会话索引和快速查找，优化数据检索效率
 * 
 * 设计特点：
 * - 接口抽象：不依赖具体的Redis实现，便于测试和替换
 * - 灵活配置：支持不同的Redis配置和连接方式
 * - 性能优化：定义高效的批量操作和异步接口
 * - 错误处理：完善的异常处理和降级机制
 *
 * @author lx
 * @date 2024-01-01
 */
public interface RedisSessionStoreInterface extends SessionStore {
    
    /**
     * 获取会话数据（不包含Channel）
     * 
     * @param sessionId 会话ID
     * @return 会话数据对象
     */
    SessionData getSessionData(String sessionId);
    
    /**
     * 存储会话数据
     * 
     * @param sessionData 会话数据
     */
    void storeSessionData(SessionData sessionData);
    
    /**
     * 检查会话是否存在
     * 
     * @param sessionId 会话ID
     * @return 是否存在
     */
    boolean existsSession(String sessionId);
    
    /**
     * 设置会话过期时间
     * 
     * @param sessionId 会话ID
     * @param expireSeconds 过期时间（秒）
     */
    void setSessionExpire(String sessionId, long expireSeconds);
    
    /**
     * 获取会话剩余过期时间
     * 
     * @param sessionId 会话ID
     * @return 剩余时间（秒），-1表示永不过期，-2表示不存在
     */
    long getSessionTTL(String sessionId);
}