package com.game.frame.data.annotation;

import com.game.frame.data.datasource.DataSourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据源切换注解
 * 
 * 功能说明：
 * - 提供声明式的数据源切换功能，支持读写分离和多数据源路由
 * - 通过AOP机制自动切换数据源，无需手动管理
 * - 支持方法级和类级的数据源指定
 * - 简化多数据源环境下的开发复杂度
 * 
 * 设计思路：
 * - 使用注解驱动的方式指定数据源类型
 * - 运行时通过反射和AOP获取注解信息
 * - 配合DataSourceAspect切面实现自动切换
 * - 支持嵌套方法的数据源继承和覆盖
 * 
 * 使用场景：
 * - 读写分离：查询方法使用从库，写入方法使用主库
 * - 业务隔离：不同业务模块使用不同数据源
 * - 性能优化：将只读操作分散到从库，减轻主库压力
 * - 数据同步：特定操作强制使用主库确保数据一致性
 * 
 * 注解属性：
 * - value：指定数据源类型，默认为MASTER主数据源
 * - 支持MASTER和SLAVE两种数据源类型
 * - 可以应用在方法或类上，方法级优先级更高
 * 
 * 技术特点：
 * - 声明式配置：通过注解声明，代码简洁明了
 * - 自动切换：AOP自动处理数据源切换逻辑
 * - 线程安全：基于ThreadLocal实现线程级数据源隔离
 * - 嵌套支持：支持方法调用链中的数据源传递
 * 
 * 使用示例：
 * ```java
 * @DataSource(DataSourceType.SLAVE)
 * public List<User> findUsers() {
 *     // 使用从库查询用户列表
 * }
 * 
 * @DataSource(DataSourceType.MASTER)
 * public void saveUser(User user) {
 *     // 使用主库保存用户
 * }
 * ```
 * 
 * 注意事项：
 * - 事务方法中的数据源切换需要注意事务边界
 * - 跨数据源的事务处理需要分布式事务支持
 * - 从库延迟可能导致读写一致性问题
 * 
 * @author lx
 * @date 2025/06/08
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSource {
    
    /**
     * 指定数据源类型
     * 
     * 功能说明：
     * - 定义当前方法或类应该使用的数据源类型
     * - 支持主库(MASTER)和从库(SLAVE)两种类型
     * - 默认使用主库，确保数据一致性和可靠性
     * 
     * 数据源选择策略：
     * - MASTER：用于写操作和需要强一致性的读操作
     *   * 所有的INSERT、UPDATE、DELETE操作
     *   * 需要实时数据的查询操作
     *   * 事务性操作和关键业务逻辑
     * 
     * - SLAVE：用于只读查询和允许轻微延迟的操作
     *   * 一般性的SELECT查询操作
     *   * 报表统计和数据分析
     *   * 允许数据轻微延迟的业务场景
     * 
     * 生效优先级：
     * 1. 方法级注解优先级最高
     * 2. 类级注解作为默认值
     * 3. 未指定时使用MASTER主库
     * 
     * 使用建议：
     * - 写操作统一使用MASTER，确保数据完整性
     * - 读操作优先使用SLAVE，提升系统性能
     * - 实时性要求高的读操作使用MASTER
     * - 批量查询和统计操作使用SLAVE
     * 
     * @return DataSourceType 数据源类型，默认为MASTER主数据源
     */
    DataSourceType value() default DataSourceType.MASTER;
}