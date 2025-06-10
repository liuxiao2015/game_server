package com.game.test.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 游戏服务器性能监控器
 * 
 * 功能说明：
 * - 实时监控游戏服务器的性能指标，包括QPS、响应时间、错误率等
 * - 提供线程安全的性能数据统计和分析功能
 * - 支持性能数据的重置和周期性统计报告
 * - 为性能优化和容量规划提供数据支持
 * 
 * 设计思路：
 * - 使用原子类实现线程安全的计数器，避免锁竞争
 * - 实时计算性能指标，支持动态性能监控
 * - 轻量级设计，监控开销最小化
 * - 提供清晰的指标展示和日志输出
 * 
 * 监控指标：
 * - QPS(每秒查询数)：衡量系统吞吐量
 * - 平均响应时间：衡量系统延迟
 * - 错误率：衡量系统稳定性
 * - 请求总数：累计请求统计
 * 
 * 使用场景：
 * - 压力测试：监控系统在高负载下的性能表现
 * - 性能优化：识别性能瓶颈和优化效果验证
 * - 容量规划：评估系统承载能力和扩容需求
 * - 生产监控：实时监控线上系统性能状态
 * 
 * 技术特点：
 * - 原子操作：使用AtomicLong确保线程安全
 * - 实时计算：基于当前数据动态计算指标
 * - 内存高效：最小化内存使用和GC压力
 * - 日志集成：与SLF4J集成，支持多种日志框架
 * 
 * 监控精度：
 * - 时间精度：毫秒级响应时间统计
 * - 计数精度：64位长整型，支持海量请求统计
 * - 比率精度：百分比计算，小数点后两位精度
 * 
 * @author lx
 * @date 2025/06/08
 */
public class PerformanceMonitor {
    
    /**
     * 日志记录器，用于输出性能统计信息
     */
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    /**
     * 请求总数计数器
     * 使用AtomicLong确保高并发环境下的线程安全
     */
    private final AtomicLong requestCount = new AtomicLong(0);
    
    /**
     * 总响应时间累计器（毫秒）
     * 用于计算平均响应时间
     */
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    /**
     * 错误请求计数器
     * 统计失败或异常的请求数量
     */
    private final AtomicLong errorCount = new AtomicLong(0);
    
    /**
     * 监控开始时间戳
     * 用于计算QPS和监控时长，使用volatile确保可见性
     */
    private volatile long startTime = System.currentTimeMillis();
    
    /**
     * 记录单次请求的性能数据
     * 
     * 功能说明：
     * - 记录请求的响应时间和成功状态
     * - 更新相关的性能计数器
     * - 为后续的性能指标计算提供数据基础
     * 
     * 业务逻辑：
     * 1. 增加请求总数计数
     * 2. 累计响应时间用于平均值计算
     * 3. 如果请求失败则增加错误计数
     * 
     * 线程安全：
     * - 使用原子操作确保高并发下的数据准确性
     * - 避免使用同步锁，减少性能开销
     * 
     * @param responseTimeMs 响应时间（毫秒）
     * @param success 请求是否成功
     */
    public void recordRequest(long responseTimeMs, boolean success) {
        // 原子性增加请求计数
        requestCount.incrementAndGet();
        
        // 原子性累加响应时间
        totalResponseTime.addAndGet(responseTimeMs);
        
        // 失败请求计数
        if (!success) {
            errorCount.incrementAndGet();
        }
    }
    
    /**
     * 获取当前QPS（每秒查询数）
     * 
     * 功能说明：
     * - 计算从监控开始到现在的平均QPS
     * - 反映系统的吞吐量能力
     * - 实时动态计算，无需预设统计周期
     * 
     * 计算逻辑：
     * 1. 获取当前时间和监控开始时间的时间差
     * 2. 将时间差转换为秒数
     * 3. 用总请求数除以秒数得到QPS
     * 4. 特殊处理：开始时间为0时返回0避免除零异常
     * 
     * @return double 当前QPS值，保留小数
     */
    public double getCurrentQPS() {
        long currentTime = System.currentTimeMillis();
        long elapsedSeconds = (currentTime - startTime) / 1000;
        
        // 避免除零异常
        if (elapsedSeconds == 0) {
            return 0;
        }
        
        return (double) requestCount.get() / elapsedSeconds;
    }
    
    /**
     * 获取平均响应时间
     * 
     * 功能说明：
     * - 计算所有请求的平均响应时间
     * - 衡量系统的响应性能
     * - 帮助识别性能瓶颈和延迟问题
     * 
     * 计算逻辑：
     * 1. 获取总响应时间和总请求数
     * 2. 计算平均值：总响应时间 / 总请求数
     * 3. 特殊处理：无请求时返回0
     * 
     * @return double 平均响应时间（毫秒）
     */
    public double getAverageResponseTime() {
        long count = requestCount.get();
        
        // 避免除零异常
        if (count == 0) {
            return 0;
        }
        
        return (double) totalResponseTime.get() / count;
    }
    
    /**
     * 获取错误率
     * 
     * 功能说明：
     * - 计算请求的错误率百分比
     * - 衡量系统的稳定性和可靠性
     * - 帮助识别系统异常和质量问题
     * 
     * 计算逻辑：
     * 1. 获取错误请求数和总请求数
     * 2. 计算百分比：(错误数 / 总数) * 100
     * 3. 特殊处理：无请求时返回0
     * 
     * @return double 错误率百分比（0-100）
     */
    public double getErrorRate() {
        long count = requestCount.get();
        
        // 避免除零异常
        if (count == 0) {
            return 0;
        }
        
        return (double) errorCount.get() / count * 100;
    }
    
    /**
     * 重置所有计数器
     * 
     * 功能说明：
     * - 清零所有性能计数器
     * - 重新设置监控开始时间
     * - 用于开启新的监控周期
     * 
     * 使用场景：
     * - 压力测试的不同阶段
     * - 定期性能报告的周期重置
     * - 系统重启或配置变更后的重新监控
     * 
     * 重置内容：
     * - 请求总数归零
     * - 总响应时间归零
     * - 错误计数归零
     * - 更新监控开始时间为当前时间
     */
    public void reset() {
        requestCount.set(0);
        totalResponseTime.set(0);
        errorCount.set(0);
        startTime = System.currentTimeMillis();
    }
    
    /**
     * 打印当前性能统计信息
     * 
     * 功能说明：
     * - 格式化输出当前的性能指标
     * - 提供人性化的性能数据展示
     * - 支持日志记录和监控展示
     * 
     * 输出内容：
     * - QPS：每秒查询数，保留两位小数
     * - 平均响应时间：毫秒单位，保留两位小数
     * - 错误率：百分比形式，保留两位小数
     * 
     * 日志级别：
     * - 使用INFO级别，便于生产环境监控
     * - 格式化输出，便于日志分析和告警
     */
    public void printStats() {
        logger.info("Performance Stats - QPS: {}, Avg Response: {}ms, Error Rate: {}%", 
                    String.format("%.2f", getCurrentQPS()),
                    String.format("%.2f", getAverageResponseTime()),
                    String.format("%.2f", getErrorRate()));
    }
    
    // Getters - 提供原始数据访问接口
    
    /**
     * 获取总请求数
     * @return long 累计请求总数
     */
    public long getRequestCount() { 
        return requestCount.get(); 
    }
    
    /**
     * 获取总响应时间
     * @return long 累计响应时间（毫秒）
     */
    public long getTotalResponseTime() { 
        return totalResponseTime.get(); 
    }
    
    /**
     * 获取错误请求数
     * @return long 累计错误请求数
     */
    public long getErrorCount() { 
        return errorCount.get(); 
    }
    
    /**
     * 获取监控开始时间
     * @return long 监控开始的时间戳
     */
    public long getStartTime() { 
        return startTime; 
    }
}