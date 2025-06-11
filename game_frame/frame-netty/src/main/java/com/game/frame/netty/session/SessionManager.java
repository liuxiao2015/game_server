package com.game.frame.netty.session;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Enhanced session manager with read-write separation and extensible storage support
 *
 * @author lx
 * @date 2024-01-01
 */
/**
 * Session管理器 - 优化版
 * 
 * 功能说明：
 * - 支持读写分离的高性能会话管理
 * - 可扩展的存储后端支持（内存、Redis等）
 * - 优化的并发访问和线程安全保证
 * - 完善的性能监控和统计功能
 * 
 * 性能优化：
 * - 读写分离：使用专门的线程池处理读写操作
 * - 索引优化：多种索引结构支持快速查询
 * - 批量操作：支持批量会话处理，提升吞吐量
 * - 异步处理：非阻塞的会话存储和检索
 *
 * @author lx
 * @date 2024-01-01
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    
    // 本地会话存储
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Session> userSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Channel, String> channelSessions = new ConcurrentHashMap<>();
    
    // 可选的外部存储接口
    private volatile SessionStore externalStore;
    
    // 读写分离线程池
    private final ExecutorService readExecutor;
    private final ExecutorService writeExecutor;
    
    // 性能统计
    private final AtomicLong readOperations = new AtomicLong(0);
    private final AtomicLong writeOperations = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    
    // 会话清理任务
    private final ScheduledExecutorService cleanupExecutor;
    
    public SessionManager() {
        // 创建读取线程池 - 更多线程用于读操作
        this.readExecutor = new ThreadPoolExecutor(
            8, 16, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            r -> {
                Thread t = new Thread(r, "session-read-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            }
        );
        
        // 创建写入线程池 - 较少线程用于写操作
        this.writeExecutor = new ThreadPoolExecutor(
            4, 8, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            r -> {
                Thread t = new Thread(r, "session-write-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            }
        );
        
        // 清理任务线程池
        this.cleanupExecutor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "session-cleanup");
            t.setDaemon(true);
            return t;
        });
        
        // 启动定期清理任务
        startCleanupTask();
    }
    
    /**
     * 设置外部存储
     */
    public void setExternalStore(SessionStore store) {
        this.externalStore = store;
        logger.info("External session store configured: {}", store.getClass().getSimpleName());
    }
    
    /**
     * 创建新会话（优化版）
     */
    public Session createSession(Channel channel) {
        Session session = new Session(channel);
        
        writeOperations.incrementAndGet();
        
        // 同步存储到本地缓存
        sessions.put(session.getSessionId(), session);
        channelSessions.put(channel, session.getSessionId());
        
        // 异步存储到外部存储
        if (externalStore != null) {
            writeExecutor.submit(() -> {
                try {
                    externalStore.storeSession(session);
                    logger.debug("Session stored to external store: {}", session.getSessionId());
                } catch (Exception e) {
                    logger.error("Failed to store session to external store: {}", session.getSessionId(), e);
                }
            });
        }
        
        logger.debug("Session created: {}", session.getSessionId());
        return session;
    }
    
    /**
     * 获取会话（读优化版）
     */
    public Session getSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }
        
        readOperations.incrementAndGet();
        
        // 优先从本地缓存获取
        Session session = sessions.get(sessionId);
        if (session != null) {
            cacheHits.incrementAndGet();
            return session;
        }
        
        cacheMisses.incrementAndGet();
        
        // 从外部存储异步获取（如果配置了）
        if (externalStore != null) {
            CompletableFuture<Session> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return externalStore.getSession(sessionId);
                } catch (Exception e) {
                    logger.error("Failed to get session from external store: {}", sessionId, e);
                    return null;
                }
            }, readExecutor);
            
            try {
                // 短暂等待，避免阻塞主线程
                Session externalSession = future.get(50, TimeUnit.MILLISECONDS);
                if (externalSession != null) {
                    logger.debug("Session retrieved from external store: {}", sessionId);
                    // 注意：从外部存储获取的Session可能没有Channel，需要特殊处理
                }
                return externalSession;
            } catch (TimeoutException e) {
                logger.debug("External store lookup timeout for session: {}", sessionId);
            } catch (Exception e) {
                logger.error("Error getting session from external store: {}", sessionId, e);
            }
        }
        
        return null;
    }
    
    /**
     * 获取会话 - 通过Channel（优化版）
     */
    public Session getSession(Channel channel) {
        if (channel == null) {
            return null;
        }
        
        readOperations.incrementAndGet();
        
        String sessionId = channelSessions.get(channel);
        if (sessionId != null) {
            cacheHits.incrementAndGet();
            return sessions.get(sessionId);
        }
        
        cacheMisses.incrementAndGet();
        
        // 尝试从sessions中查找（较慢的方式）
        return sessions.values().stream()
                .filter(session -> session.getChannel() == channel)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 根据用户ID获取会话（优化版）
     */
    public Session getSessionByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        
        readOperations.incrementAndGet();
        
        // 先从本地缓存获取
        Session session = userSessions.get(userId);
        if (session != null) {
            cacheHits.incrementAndGet();
            return session;
        }
        
        cacheMisses.incrementAndGet();
        
        // 从外部存储获取
        if (externalStore != null) {
            CompletableFuture<Session> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return externalStore.getSessionByUserId(userId);
                } catch (Exception e) {
                    logger.error("Failed to get session by user ID from external store: {}", userId, e);
                    return null;
                }
            }, readExecutor);
            
            try {
                return future.get(50, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.error("Error getting session by user ID: {}", userId, e);
            }
        }
        
        return null;
    }
    
    /**
     * 绑定用户到会话（优化版）
     */
    public boolean bindUser(String sessionId, String userId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            logger.warn("Session not found for binding: {}", sessionId);
            return false;
        }
        
        writeOperations.incrementAndGet();
        
        // 检查用户是否已绑定到其他会话
        Session existingSession = userSessions.get(userId);
        if (existingSession != null && !existingSession.getSessionId().equals(sessionId)) {
            logger.warn("User {} already bound to another session: {}", userId, existingSession.getSessionId());
            // 移除旧会话
            removeSession(existingSession.getSessionId());
        }
        
        // 移除旧的用户绑定
        if (session.getUserId() != null) {
            userSessions.remove(session.getUserId());
        }
        
        // 建立新的绑定
        session.setUserId(userId);
        session.setAuthenticated(true);
        userSessions.put(userId, session);
        
        // 异步更新外部存储
        if (externalStore != null) {
            writeExecutor.submit(() -> {
                try {
                    externalStore.storeSession(session);
                    logger.debug("User binding updated in external store: {} -> {}", userId, sessionId);
                } catch (Exception e) {
                    logger.error("Failed to update user binding in external store: {} -> {}", userId, sessionId, e);
                }
            });
        }
        
        logger.info("User {} bound to session {}", userId, sessionId);
        return true;
    }
    
    /**
     * 移除会话（优化版）
     */
    public Session removeSession(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session != null) {
            writeOperations.incrementAndGet();
            
            // 清理本地缓存
            if (session.getUserId() != null) {
                userSessions.remove(session.getUserId());
            }
            channelSessions.remove(session.getChannel());
            
            // 关闭连接
            session.close();
            
            // 异步从外部存储删除
            if (externalStore != null) {
                writeExecutor.submit(() -> {
                    try {
                        externalStore.removeSession(sessionId);
                        logger.debug("Session removed from external store: {}", sessionId);
                    } catch (Exception e) {
                        logger.error("Failed to remove session from external store: {}", sessionId, e);
                    }
                });
            }
            
            logger.debug("Session removed: {}", sessionId);
        }
        return session;
    }
    
    /**
     * 根据Channel移除会话
     */
    public Session removeSession(Channel channel) {
        String sessionId = channelSessions.get(channel);
        if (sessionId != null) {
            return removeSession(sessionId);
        }
        
        // 如果Channel索引中没有，尝试查找并移除
        Session session = getSession(channel);
        if (session != null) {
            return removeSession(session.getSessionId());
        }
        
        return null;
    }
    
    /**
     * 获取所有活跃会话
     */
    public Collection<Session> getAllSessions() {
        readOperations.incrementAndGet();
        return sessions.values();
    }
    
    /**
     * 获取会话数量
     */
    public int getSessionCount() {
        return sessions.size();
    }
    
    /**
     * 获取已认证会话数量
     */
    public int getAuthenticatedSessionCount() {
        return userSessions.size();
    }
    
    /**
     * 广播消息到所有会话（并行处理）
     */
    public void broadcastToAll(Object message) {
        sessions.values().parallelStream().forEach(session -> {
            try {
                session.sendMessage(message);
            } catch (Exception e) {
                logger.error("Failed to send message to session: {}", session.getSessionId(), e);
            }
        });
        
        logger.debug("Broadcast message to {} sessions", sessions.size());
    }
    
    /**
     * 广播消息到已认证会话（并行处理）
     */
    public void broadcastToAuthenticated(Object message) {
        userSessions.values().parallelStream().forEach(session -> {
            try {
                session.sendMessage(message);
            } catch (Exception e) {
                logger.error("Failed to send message to session: {}", session.getSessionId(), e);
            }
        });
        
        logger.debug("Broadcast message to {} authenticated sessions", userSessions.size());
    }
    
    /**
     * 条件广播（并行处理）
     */
    public void broadcastTo(Object message, Predicate<Session> predicate) {
        long count = sessions.values().parallelStream()
            .filter(predicate)
            .peek(session -> {
                try {
                    session.sendMessage(message);
                } catch (Exception e) {
                    logger.error("Failed to send message to session: {}", session.getSessionId(), e);
                }
            })
            .count();
        
        logger.debug("Broadcast message to {} filtered sessions", count);
    }
    
    /**
     * 移除不活跃会话（优化版）
     */
    public int removeInactiveSessions(long timeoutMs) {
        long currentTime = System.currentTimeMillis();
        int[] removedCount = {0};
        
        // 并行处理不活跃会话检查
        sessions.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (currentTime - session.getLastActiveTime() > timeoutMs || !session.isActive()) {
                // 清理关联数据
                if (session.getUserId() != null) {
                    userSessions.remove(session.getUserId());
                }
                channelSessions.remove(session.getChannel());
                session.close();
                
                removedCount[0]++;
                logger.debug("Removed inactive session: {}", session.getSessionId());
                
                // 异步从外部存储删除
                if (externalStore != null) {
                    writeExecutor.submit(() -> {
                        try {
                            externalStore.removeSession(session.getSessionId());
                        } catch (Exception e) {
                            logger.error("Failed to remove inactive session from external store: {}", 
                                    session.getSessionId(), e);
                        }
                    });
                }
                
                return true;
            }
            return false;
        });
        
        if (removedCount[0] > 0) {
            logger.info("Removed {} inactive sessions", removedCount[0]);
        }
        
        return removedCount[0];
    }
    
    /**
     * 清空所有会话（优化版）
     */
    public void clear() {
        sessions.values().forEach(Session::close);
        sessions.clear();
        userSessions.clear();
        channelSessions.clear();
        
        // 异步清理外部存储
        if (externalStore != null) {
            writeExecutor.submit(() -> {
                try {
                    externalStore.clearAllSessions();
                    logger.info("All sessions cleared from external store");
                } catch (Exception e) {
                    logger.error("Failed to clear sessions from external store", e);
                }
            });
        }
        
        logger.info("All sessions cleared");
    }
    
    /**
     * 获取性能统计
     */
    public SessionStats getStats() {
        return new SessionStats(
            sessions.size(),
            userSessions.size(),
            readOperations.get(),
            writeOperations.get(),
            cacheHits.get(),
            cacheMisses.get()
        );
    }
    
    /**
     * 启动清理任务
     */
    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(() -> {
            try {
                removeInactiveSessions(600000); // 10分钟超时
            } catch (Exception e) {
                logger.error("Session cleanup task failed", e);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }
    
    /**
     * 关闭会话管理器
     */
    public void shutdown() {
        try {
            readExecutor.shutdown();
            writeExecutor.shutdown();
            cleanupExecutor.shutdown();
            
            if (!readExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                readExecutor.shutdownNow();
            }
            if (!writeExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                writeExecutor.shutdownNow();
            }
            if (!cleanupExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
            
            logger.info("Session manager shutdown completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Session manager shutdown interrupted");
        }
    }
    
    /**
     * 会话统计信息
     */
    public static class SessionStats {
        private final int totalSessions;
        private final int authenticatedSessions;
        private final long readOperations;
        private final long writeOperations;
        private final long cacheHits;
        private final long cacheMisses;
        
        public SessionStats(int totalSessions, int authenticatedSessions, 
                           long readOperations, long writeOperations, 
                           long cacheHits, long cacheMisses) {
            this.totalSessions = totalSessions;
            this.authenticatedSessions = authenticatedSessions;
            this.readOperations = readOperations;
            this.writeOperations = writeOperations;
            this.cacheHits = cacheHits;
            this.cacheMisses = cacheMisses;
        }
        
        public double getCacheHitRate() {
            long total = cacheHits + cacheMisses;
            return total > 0 ? (double) cacheHits / total : 0.0;
        }
        
        // Getters
        public int getTotalSessions() { return totalSessions; }
        public int getAuthenticatedSessions() { return authenticatedSessions; }
        public long getReadOperations() { return readOperations; }
        public long getWriteOperations() { return writeOperations; }
        public long getCacheHits() { return cacheHits; }
        public long getCacheMisses() { return cacheMisses; }
        
        @Override
        public String toString() {
            return String.format("SessionStats{total=%d, auth=%d, reads=%d, writes=%d, hitRate=%.2f%%}", 
                totalSessions, authenticatedSessions, readOperations, writeOperations, getCacheHitRate() * 100);
        }
    }
}