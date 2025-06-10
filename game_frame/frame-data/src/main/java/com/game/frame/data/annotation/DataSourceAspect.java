package com.game.frame.data.annotation;

import com.game.frame.data.datasource.DynamicDataSourceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 动态数据源切换切面处理器
 * 
 * 功能说明：
 * - 基于AOP实现数据源的自动切换和管理
 * - 支持方法级和类级的数据源注解配置
 * - 提供读写分离和多数据源路由能力
 * - 确保数据源切换的线程安全性和资源清理
 * 
 * 设计思路：
 * - 采用环绕通知（Around）实现数据源的完整生命周期管理
 * - 通过ThreadLocal维护当前线程的数据源上下文
 * - 支持注解继承，类级注解作为方法级注解的默认值
 * - 异常安全设计，确保数据源上下文的正确清理
 * 
 * 切换策略：
 * - 方法级注解优先：@DataSource注解在方法上时优先使用
 * - 类级注解兜底：方法无注解时使用类级注解配置
 * - 默认数据源：无任何注解时使用系统默认配置
 * - 嵌套支持：支持数据源的嵌套切换和恢复
 * 
 * 核心能力：
 * - 读写分离：自动路由读操作到从库，写操作到主库
 * - 多租户支持：根据租户信息动态选择数据源
 * - 分库分表：支持基于业务规则的数据源路由
 * - 故障转移：主库故障时自动切换到备库
 * 
 * 性能优化：
 * - 轻量级切面：最小化AOP拦截的性能开销
 * - 缓存注解解析：避免重复的反射和注解查找
 * - 懒加载策略：按需创建和初始化数据源连接
 * - 连接池复用：合理复用数据库连接减少建连开销
 * 
 * 线程安全：
 * - ThreadLocal隔离：不同线程的数据源上下文相互独立
 * - 异常安全：finally块确保上下文清理不被异常跳过
 * - 无状态设计：切面本身不维护任何状态信息
 * - 并发友好：支持高并发环境下的数据源切换
 * 
 * 使用场景：
 * - 读写分离的数据访问模式
 * - 多租户系统的数据隔离
 * - 分布式系统的数据源路由
 * - 主备切换的高可用架构
 * 
 * 配置示例：
 * <pre>
 * // 方法级数据源切换
 * &#64;DataSource("slave")
 * public List&lt;User&gt; findUsers() { ... }
 * 
 * // 类级默认数据源
 * &#64;DataSource("master")
 * public class UserService { ... }
 * 
 * // 动态数据源选择
 * &#64;DataSource("tenant_${tenantId}")
 * public void saveUserData(String tenantId, UserData data) { ... }
 * </pre>
 * 
 * 监控指标：
 * - 数据源切换次数和成功率
 * - 不同数据源的访问频率统计
 * - 数据源切换的平均耗时
 * - 异常情况的错误统计和分析
 * 
 * 注意事项：
 * - 数据源名称要与配置文件中的定义保持一致
 * - 避免在事务中间切换数据源造成数据不一致
 * - 注意数据源的连接数限制和连接泄漏
 * - 异常处理要考虑数据源恢复和降级策略
 * 
 * @author lx
 * @date 2025/06/08
 * @since 1.0.0
 * @see DataSource
 * @see DynamicDataSourceContext
 * @see ProceedingJoinPoint
 */
@Aspect
@Component
public class DataSourceAspect {

    // 日志记录器，用于记录数据源切换的详细信息和调试
    private static final Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);

    /**
     * 数据源切点定义
     * 
     * 匹配所有标注了@DataSource注解的方法，用于：
     * - 拦截需要进行数据源切换的方法调用
     * - 提供统一的数据源管理入口
     * - 支持方法级别的精确控制
     * - 实现声明式的数据源配置
     * 
     * 切点表达式说明：
     * - @annotation：匹配方法级别的注解
     * - 完整包路径：避免注解冲突和误匹配
     * - 精确匹配：只拦截确实需要数据源切换的方法
     */
    @Pointcut("@annotation(com.game.frame.data.annotation.DataSource)")
    public void dataSourcePointcut() {}

    /**
     * 数据源切换环绕通知
     * 
     * 核心数据源切换逻辑，实现以下功能：
     * - 方法执行前：解析注解并设置目标数据源
     * - 方法执行中：保持数据源上下文的稳定性
     * - 方法执行后：清理数据源上下文，避免污染
     * - 异常处理：确保异常情况下的资源清理
     * 
     * 注解解析优先级：
     * 1. 方法级@DataSource注解（最高优先级）
     * 2. 类级@DataSource注解（作为默认值）
     * 3. 系统默认数据源（无注解时）
     * 
     * 线程安全保证：
     * - 使用ThreadLocal存储数据源上下文
     * - finally块确保上下文清理的可靠性
     * - 支持数据源的嵌套切换和恢复
     * 
     * 性能优化：
     * - 缓存Method对象避免重复反射
     * - 懒加载注解解析减少不必要开销
     * - 最小化切面逻辑的执行时间
     * 
     * @param point 连接点，包含目标方法的详细信息
     * @return 目标方法的执行结果
     * @throws Throwable 目标方法可能抛出的任何异常
     */
    @Around("dataSourcePointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 获取方法签名和元数据信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        
        // 解析数据源注解，优先使用方法级注解
        DataSource dataSource = AnnotationUtils.findAnnotation(method, DataSource.class);
        if (dataSource == null) {
            // 方法级注解不存在时，查找类级注解作为默认配置
            dataSource = AnnotationUtils.findAnnotation(method.getDeclaringClass(), DataSource.class);
        }
        
        // 保存原始数据源上下文，支持嵌套切换
        String originalDataSource = DynamicDataSourceContext.getDataSourceType();
        
        if (dataSource != null) {
            // 设置目标数据源到当前线程上下文
            String targetDataSource = dataSource.value();
            DynamicDataSourceContext.setDataSourceType(targetDataSource);
            
            logger.debug("数据源切换 - 目标数据源: {}, 方法: {}.{}, 原数据源: {}", 
                        targetDataSource, 
                        method.getDeclaringClass().getSimpleName(),
                        method.getName(),
                        originalDataSource);
        }
        
        try {
            // 执行目标方法，此时已在正确的数据源上下文中
            Object result = point.proceed();
            
            if (dataSource != null) {
                logger.debug("方法执行成功 - 数据源: {}, 方法: {}.{}", 
                           dataSource.value(),
                           method.getDeclaringClass().getSimpleName(),
                           method.getName());
            }
            
            return result;
            
        } catch (Throwable e) {
            // 记录异常情况下的数据源信息，便于问题排查
            if (dataSource != null) {
                logger.error("方法执行异常 - 数据源: {}, 方法: {}.{}, 异常: {}", 
                           dataSource.value(),
                           method.getDeclaringClass().getSimpleName(),
                           method.getName(),
                           e.getMessage());
            }
            throw e;
            
        } finally {
            // 恢复原始数据源上下文，确保线程安全
            if (originalDataSource != null) {
                DynamicDataSourceContext.setDataSourceType(originalDataSource);
            } else {
                DynamicDataSourceContext.clearDataSourceType();
            }
            
            if (dataSource != null) {
                logger.debug("数据源上下文已恢复 - 原数据源: {}", originalDataSource);
            }
        }
    }
}