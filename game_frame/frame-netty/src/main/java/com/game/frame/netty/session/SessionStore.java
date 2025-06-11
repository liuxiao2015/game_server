package com.game.frame.netty.session;

import java.util.Set;

/**
 * Session storage interface
 * 
 * 功能说明：
 * - 定义会话存储的统一接口，支持多种存储实现（内存、Redis等）
 * - 提供会话的基本CRUD操作，支持分布式会话管理
 * - 抽象存储细节，便于切换不同的存储后端
 * - 支持会话查询、索引和批量操作
 * 
 * 设计理念：
 * - 接口分离原则：将会话管理逻辑与存储实现分离
 * - 扩展性：支持不同的存储后端实现
 * - 性能优化：定义高效的查询和更新接口
 * - 分布式支持：考虑分布式环境的数据一致性需求
 *
 * @author lx
 * @date 2024-01-01
 */
public interface SessionStore {
    
    /**
     * 存储会话数据
     * 
     * @param session 会话对象
     */
    void storeSession(Session session);
    
    /**
     * 根据会话ID获取会话
     * 
     * @param sessionId 会话ID
     * @return 会话对象，如果不存在则返回null
     */
    Session getSession(String sessionId);
    
    /**
     * 根据用户ID获取会话
     * 
     * @param userId 用户ID
     * @return 会话对象，如果不存在则返回null
     */
    Session getSessionByUserId(String userId);
    
    /**
     * 删除会话
     * 
     * @param sessionId 会话ID
     */
    void removeSession(String sessionId);
    
    /**
     * 更新会话活跃时间
     * 
     * @param sessionId 会话ID
     * @param activeTime 活跃时间戳
     */
    void updateSessionActiveTime(String sessionId, long activeTime);
    
    /**
     * 获取所有会话ID
     * 
     * @return 会话ID集合
     */
    Set<String> getAllSessionIds();
    
    /**
     * 获取会话总数
     * 
     * @return 会话数量
     */
    int getSessionCount();
    
    /**
     * 清空所有会话
     */
    void clearAllSessions();
}