package com.game.frame.data.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源实现类
 * 
 * 功能说明：
 * - 继承Spring的AbstractRoutingDataSource，实现运行时数据源切换
 * - 根据当前线程上下文自动选择合适的数据源(主库或从库)
 * - 支持读写分离架构，优化数据库负载分配
 * - 提供透明的数据源切换，业务代码无需感知底层数据源变化
 * 
 * 设计思路：
 * - 基于ThreadLocal的上下文管理，确保线程安全
 * - 使用策略模式动态选择数据源，灵活且可扩展
 * - 默认主数据源策略，确保数据一致性和系统稳定性
 * - 与Spring事务管理器无缝集成
 * 
 * 工作原理：
 * 1. 业务方法通过@DataSource注解或编程方式设置数据源类型
 * 2. AOP切面将数据源类型存储到ThreadLocal中
 * 3. 数据源路由时调用determineCurrentLookupKey()方法
 * 4. 根据ThreadLocal中的类型返回对应的数据源标识
 * 5. Spring根据标识选择并返回对应的数据源实例
 * 
 * 使用场景：
 * - 读写分离：写操作使用主库，读操作使用从库
 * - 负载均衡：将查询压力分散到多个从库
 * - 业务隔离：不同业务模块使用不同的数据源
 * - 多租户架构：根据租户信息选择对应的数据源
 * 
 * 技术特点：
 * - 线程安全：基于ThreadLocal实现线程隔离
 * - 自动切换：根据上下文自动选择数据源，无需手动管理
 * - 事务支持：与Spring事务管理器完全兼容
 * - 容错机制：未设置数据源类型时默认使用主库
 * 
 * 性能优化：
 * - 避免了手动管理多个DataSource的复杂性
 * - 减少主库压力，提升系统整体性能
 * - 支持连接池复用，降低连接建立开销
 * 
 * @author lx
 * @date 2025/06/08
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    /**
     * 确定当前查找键
     * 
     * 功能说明：
     * - AbstractRoutingDataSource的核心方法，用于确定当前应该使用哪个数据源
     * - 在每次获取数据库连接时被调用，动态返回数据源标识
     * - 基于ThreadLocal上下文获取当前线程的数据源类型
     * 
     * 业务逻辑：
     * 1. 从DynamicDataSourceContext获取当前线程的数据源类型
     * 2. 如果上下文中没有设置数据源类型，则默认返回MASTER主数据源
     * 3. 如果已设置数据源类型，则返回对应的数据源标识
     * 4. 返回的标识会用于从targetDataSources中查找对应的数据源实例
     * 
     * 安全策略：
     * - 默认主数据源：确保在没有明确指定时使用最可靠的数据源
     * - 避免空指针：对null值进行检查和处理
     * - 线程安全：基于ThreadLocal，不同线程间相互独立
     * 
     * 使用时机：
     * - 每次执行SQL语句前获取数据库连接时
     * - Spring事务开启时确定事务数据源
     * - JPA EntityManager创建时选择数据源
     * 
     * 注意事项：
     * - 此方法调用频率很高，需要保证执行效率
     * - 返回值必须与配置的targetDataSources中的key匹配
     * - 线程结束后需要清理ThreadLocal，避免内存泄漏
     * 
     * @return Object 数据源查找键，对应DataSourceType枚举值
     */
    @Override
    protected Object determineCurrentLookupKey() {
        // 从当前线程上下文获取数据源类型
        DataSourceType dataSourceType = DynamicDataSourceContext.getDataSourceType();
        
        if (dataSourceType == null) {
            // 默认使用主数据源，确保数据一致性和系统稳定性
            return DataSourceType.MASTER;
        }
        
        // 返回指定的数据源类型
        return dataSourceType;
    }
}