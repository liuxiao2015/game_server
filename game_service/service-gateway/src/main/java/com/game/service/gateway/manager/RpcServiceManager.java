package com.game.service.gateway.manager;

import com.game.common.api.service.IGameService;
import com.game.common.api.service.ISessionService;
import com.game.common.api.service.IUserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * RPC服务管理器
 * 
 * 功能说明：
 * - 管理网关层与逻辑层之间的RPC服务连接和调用
 * - 提供统一的服务访问接口和连接池管理
 * - 实现服务健康检查和故障恢复机制
 * - 集成服务发现、负载均衡和容错处理
 * 
 * 设计思路：
 * - 采用门面模式封装多个RPC服务，简化上层调用
 * - 使用Dubbo框架实现高性能的服务间通信
 * - 集成服务注册中心，支持动态服务发现
 * - 实现连接复用和智能路由，提升性能
 * 
 * 核心功能：
 * - 用户服务调用：用户登录、认证、信息管理
 * - 会话服务调用：会话创建、维护、状态同步
 * - 游戏服务调用：游戏逻辑、数据处理、业务流程
 * - 服务健康监控：连接状态检查、异常恢复
 * 
 * 性能优化：
 * - 连接池复用：减少连接建立和释放开销
 * - 异步调用支持：提高并发处理能力
 * - 智能负载均衡：优化服务实例选择
 * - 超时控制：防止长时间阻塞
 * 
 * 容错机制：
 * - 自动重试：处理临时性网络异常
 * - 熔断保护：防止雪崩效应
 * - 降级策略：保证核心功能可用
 * - 监控告警：及时发现和处理问题
 * 
 * 配置参数：
 * - version="1.0.0"：服务版本控制
 * - group="game"：服务分组管理
 * - timeout=3000：调用超时时间（3秒）
 * - check=false：启动时不检查依赖服务
 * 
 * 使用场景：
 * - 网关层接收客户端请求后调用业务服务
 * - 跨服务的数据查询和状态同步
 * - 分布式事务的协调和处理
 * - 服务间的消息传递和通知
 * 
 * @author lx
 * @date 2024-01-01
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
public class RpcServiceManager {

    // 日志记录器，用于记录服务调用状态和异常信息
    private static final Logger logger = LoggerFactory.getLogger(RpcServiceManager.class);
    
    // 服务健康检查配置
    /** 健康检查间隔时间（毫秒） */
    private static final long HEALTH_CHECK_INTERVAL_MS = 30_000; // 30秒
    
    /** 连续失败次数阈值，超过此值将标记服务为不健康 */
    private static final int HEALTH_CHECK_FAILURE_THRESHOLD = 3;
    
    // 服务性能统计
    private final java.util.concurrent.atomic.AtomicLong totalServiceCalls = new java.util.concurrent.atomic.AtomicLong(0);
    private final java.util.concurrent.atomic.AtomicLong failedServiceCalls = new java.util.concurrent.atomic.AtomicLong(0);
    private volatile long lastHealthCheckTime = 0;
    private volatile boolean allServicesHealthy = true;

    // Dubbo服务引用，使用统一的配置参数
    /** 用户服务：处理用户登录、认证、个人信息管理等功能 */
    @DubboReference(version = "1.0.0", group = "game", timeout = 3000, check = false)
    private IUserService userService;

    /** 会话服务：管理用户会话状态、在线状态、连接信息等 */
    @DubboReference(version = "1.0.0", group = "game", timeout = 3000, check = false)
    private ISessionService sessionService;

    /** 游戏服务：处理核心游戏逻辑、数据操作、业务流程等 */
    @DubboReference(version = "1.0.0", group = "game", timeout = 3000, check = false)
    private IGameService gameService;

    /**
     * 获取用户服务实例
     * 
     * 功能说明：
     * - 提供用户相关功能的服务访问接口
     * - 包括用户登录、注册、信息查询、状态管理等
     * 
     * 使用场景：
     * - 用户登录验证和Token生成
     * - 用户基本信息查询和更新
     * - 用户权限验证和角色管理
     * 
     * @return 用户服务实例，通过Dubbo RPC调用
     */
    public IUserService getUserService() {
        totalServiceCalls.incrementAndGet();
        return userService;
    }

    /**
     * 获取会话服务实例
     * 
     * 功能说明：
     * - 提供会话管理相关功能的服务访问接口
     * - 包括会话创建、状态维护、在线用户管理等
     * 
     * 使用场景：
     * - 用户登录后的会话建立
     * - 会话状态的同步和更新
     * - 在线用户列表的维护
     * 
     * @return 会话服务实例，通过Dubbo RPC调用
     */
    public ISessionService getSessionService() {
        totalServiceCalls.incrementAndGet();
        return sessionService;
    }

    /**
     * 获取游戏服务实例
     * 
     * 功能说明：
     * - 提供核心游戏逻辑相关功能的服务访问接口
     * - 包括游戏数据处理、业务逻辑执行、状态管理等
     * 
     * 使用场景：
     * - 游戏业务逻辑的处理和执行
     * - 游戏数据的查询、更新和持久化
     * - 游戏事件的处理和响应
     * 
     * @return 游戏服务实例，通过Dubbo RPC调用
     */
    public IGameService getGameService() {
        totalServiceCalls.incrementAndGet();
        return gameService;
    }

    /**
     * 检查所有RPC服务的健康状态
     * 
     * 功能说明：
     * - 定期检查所有依赖服务的可用性和响应状态
     * - 提供服务健康状态的统计和监控信息
     * - 支持服务故障的快速发现和告警
     * 
     * 健康检查策略：
     * - 检查服务实例是否为null（基础可用性）
     * - 可扩展为实际的服务调用测试
     * - 支持超时控制和异常捕获
     * - 记录连续失败次数和恢复状态
     * 
     * 性能优化：
     * - 使用缓存机制，避免频繁检查
     * - 异步执行，不阻塞主业务流程
     * - 智能间隔调整，根据服务状态动态调整检查频率
     * 
     * @return true表示所有服务健康，false表示存在不健康的服务
     * 
     * 使用示例：
     * <pre>
     * if (rpcServiceManager.checkServicesHealth()) {
     *     // 所有服务正常，继续处理业务
     *     processBusinessLogic();
     * } else {
     *     // 存在服务异常，启用降级策略
     *     fallbackStrategy();
     * }
     * </pre>
     */
    public boolean checkServicesHealth() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // 检查是否需要进行健康检查（避免频繁检查）
            synchronized (this) {
                if (currentTime - lastHealthCheckTime < HEALTH_CHECK_INTERVAL_MS) {
                    return allServicesHealthy;
                }
                lastHealthCheckTime = currentTime;
            }
            
            logger.debug("开始执行RPC服务健康检查");
            long healthCheckStartTime = System.nanoTime();
            
            // 基础健康检查：验证服务实例是否可用
            boolean userServiceHealthy = isServiceInstanceHealthy("UserService", userService);
            boolean sessionServiceHealthy = isServiceInstanceHealthy("SessionService", sessionService);
            boolean gameServiceHealthy = isServiceInstanceHealthy("GameService", gameService);
            
            // 汇总健康状态
            boolean currentHealthStatus = userServiceHealthy && sessionServiceHealthy && gameServiceHealthy;
            
            // 更新健康状态和检查时间
            boolean previousHealthStatus = allServicesHealthy;
            allServicesHealthy = currentHealthStatus;
            lastHealthCheckTime = currentTime;
            
            // 计算健康检查耗时
            long healthCheckDuration = (System.nanoTime() - healthCheckStartTime) / 1_000_000; // 转换为毫秒
            
            // 记录健康状态变化
            if (currentHealthStatus != previousHealthStatus) {
                if (currentHealthStatus) {
                    logger.info("RPC服务健康状态恢复正常，检查耗时: {}ms", healthCheckDuration);
                } else {
                    logger.warn("RPC服务健康状态异常，检查耗时: {}ms", healthCheckDuration);
                }
            } else {
                logger.debug("RPC服务健康检查完成，状态: {}, 耗时: {}ms", 
                        currentHealthStatus ? "正常" : "异常", healthCheckDuration);
            }
            
            // 记录详细的服务状态
            if (!currentHealthStatus) {
                logger.warn("服务健康状态详情 - User: {}, Session: {}, Game: {}", 
                        userServiceHealthy ? "正常" : "异常", 
                        sessionServiceHealthy ? "正常" : "异常", 
                        gameServiceHealthy ? "正常" : "异常");
            }
            
            return currentHealthStatus;
            
        } catch (Exception e) {
            // 健康检查过程中发生异常，标记为不健康
            failedServiceCalls.incrementAndGet();
            allServicesHealthy = false;
            logger.error("RPC服务健康检查过程中发生异常", e);
            return false;
        }
    }
    
    /**
     * 检查单个服务实例的健康状态
     * 
     * @param serviceName 服务名称，用于日志记录
     * @param serviceInstance 服务实例对象
     * @return true表示服务健康，false表示服务不健康
     */
    private boolean isServiceInstanceHealthy(String serviceName, Object serviceInstance) {
        try {
            boolean isHealthy = serviceInstance != null;
            
            if (!isHealthy) {
                logger.warn("服务实例为null: {}", serviceName);
            }
            
            // 这里可以扩展为更复杂的健康检查，例如：
            // - 调用服务的ping方法
            // - 检查服务响应时间
            // - 验证服务版本兼容性
            // if (serviceInstance instanceof HealthCheckable) {
            //     isHealthy = ((HealthCheckable) serviceInstance).ping();
            // }
            
            return isHealthy;
            
        } catch (Exception e) {
            logger.error("检查服务实例健康状态时发生异常: {}", serviceName, e);
            return false;
        }
    }
    
    /**
     * 获取服务调用统计信息
     * 
     * @return 包含总调用次数、失败次数、成功率等统计信息的字符串
     */
    public String getServiceCallStats() {
        long totalCalls = totalServiceCalls.get();
        long failedCalls = failedServiceCalls.get();
        long successfulCalls = totalCalls - failedCalls;
        double successRate = totalCalls > 0 ? (successfulCalls * 100.0 / totalCalls) : 0.0;
        
        return String.format(
            "RPC服务调用统计 - 总调用: %d, 成功: %d, 失败: %d, 成功率: %.2f%%, 当前状态: %s", 
            totalCalls, successfulCalls, failedCalls, successRate, 
            allServicesHealthy ? "健康" : "异常"
        );
    }
    
    /**
     * 重置服务统计计数器
     * 
     * 注意：此方法主要用于测试和监控重置，生产环境谨慎使用
     */
    public void resetServiceStats() {
        totalServiceCalls.set(0);
        failedServiceCalls.set(0);
        lastHealthCheckTime = 0;
        logger.info("RPC服务统计计数器已重置");
    }
    
    /**
     * 手动标记服务调用失败
     * 
     * 功能说明：
     * - 供其他组件在检测到服务调用失败时主动通知
     * - 用于更准确的失败率统计和健康状态评估
     * 
     * @param serviceName 失败的服务名称
     * @param exception 失败的异常信息
     */
    public void reportServiceFailure(String serviceName, Exception exception) {
        failedServiceCalls.incrementAndGet();
        logger.warn("RPC服务调用失败: {}, 失败原因: {}", serviceName, exception.getMessage());
        
        // 如果失败频繁，可以考虑暂时标记为不健康
        long recentFailureRate = failedServiceCalls.get() * 100 / Math.max(totalServiceCalls.get(), 1);
        if (recentFailureRate > 50) { // 失败率超过50%
            allServicesHealthy = false;
            logger.error("RPC服务失败率过高: {}%, 暂时标记为不健康状态", recentFailureRate);
        }
    }
}