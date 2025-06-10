package com.game.frame.data.config;

import com.game.frame.data.datasource.DataSourceType;
import com.game.frame.data.datasource.DynamicDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源配置类
 * 
 * 功能说明：
 * - 配置游戏服务器的主从数据库连接，支持读写分离和负载均衡
 * - 使用HikariCP连接池，提供高性能的数据库连接管理
 * - 实现动态数据源切换，根据业务需求自动选择合适的数据源
 * - 集成Spring事务管理，确保数据一致性和ACID特性
 * 
 * 设计思路：
 * - 主从分离架构：写操作使用主库，读操作使用从库，提升性能和可用性
 * - 动态数据源：运行时根据注解或上下文自动切换数据源
 * - 连接池优化：使用HikariCP并进行性能调优，确保高并发下的稳定性
 * - 配置外部化：数据源配置通过配置文件管理，支持多环境部署
 * 
 * 使用场景：
 * - 游戏用户数据的读写操作，支持大量并发访问
 * - 游戏配置和元数据的查询，利用读写分离提升性能
 * - 分布式事务处理，确保跨服务的数据一致性
 * - 数据库故障切换，提供高可用性保障
 * 
 * 技术特点：
 * - HikariCP高性能连接池，低延迟和高吞吐量
 * - 读写分离，优化数据库负载分配
 * - 连接泄露检测和自动恢复机制
 * - 预编译语句缓存，提升SQL执行效率
 * 
 * 性能优化：
 * - 连接池大小动态调整，平衡资源使用和性能
 * - 预编译语句缓存，减少SQL解析开销
 * - 连接有效性检测和超时控制
 * - 批量操作优化和服务器端预编译
 * 
 * @author lx
 * @date 2025/06/08
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    /**
     * 主数据源配置构建器
     * 
     * 功能说明：
     * - 创建主数据源的HikariCP配置对象
     * - 从配置文件中读取spring.datasource.master节点的配置信息
     * - 用于处理写操作和强一致性要求的读操作
     * 
     * @return HikariConfig 主数据源配置对象
     */
    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public HikariConfig masterDataSourceConfig() {
        return new HikariConfig();
    }

    /**
     * 从数据源配置构建器
     * 
     * 功能说明：
     * - 创建从数据源的HikariCP配置对象
     * - 从配置文件中读取spring.datasource.slave节点的配置信息
     * - 主要用于只读查询操作，分担主库的查询压力
     * 
     * @return HikariConfig 从数据源配置对象
     */
    @Bean
    @ConfigurationProperties("spring.datasource.slave")
    public HikariConfig slaveDataSourceConfig() {
        return new HikariConfig();
    }

    /**
     * 创建主数据源实例
     * 
     * 业务逻辑：
     * 1. 获取主数据源配置对象
     * 2. 应用性能优化配置
     * 3. 创建HikariDataSource实例
     * 
     * 使用场景：
     * - 所有写操作(INSERT、UPDATE、DELETE)
     * - 需要强一致性的读操作
     * - 事务性操作和关键业务逻辑
     * 
     * @return DataSource 主数据源实例
     */
    @Bean
    public DataSource masterDataSource() {
        HikariConfig config = masterDataSourceConfig();
        optimizeHikariConfig(config);
        return new HikariDataSource(config);
    }

    /**
     * 创建从数据源实例
     * 
     * 业务逻辑：
     * 1. 获取从数据源配置对象
     * 2. 应用性能优化配置
     * 3. 创建HikariDataSource实例
     * 
     * 使用场景：
     * - 只读查询操作(SELECT)
     * - 报表统计和数据分析
     * - 允许轻微延迟的查询操作
     * 
     * @return DataSource 从数据源实例
     */
    @Bean
    public DataSource slaveDataSource() {
        HikariConfig config = slaveDataSourceConfig();
        optimizeHikariConfig(config);
        return new HikariDataSource(config);
    }

    /**
     * 创建动态数据源
     * 
     * 功能说明：
     * - 创建支持运行时切换的动态数据源
     * - 根据业务逻辑或注解自动选择主库或从库
     * - 作为主要的数据源Bean被Spring容器管理
     * 
     * 业务逻辑：
     * 1. 创建DynamicDataSource实例
     * 2. 配置目标数据源映射(主库和从库)
     * 3. 设置默认数据源为主库
     * 4. 支持运行时动态切换数据源
     * 
     * 切换策略：
     * - 默认使用主数据源确保数据一致性
     * - 通过@DataSource注解指定特定数据源
     * - 支持编程式的数据源切换
     * 
     * @return DataSource 动态数据源实例
     */
    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        
        // 配置目标数据源映射
        Map<Object, Object> targetDataSources = new HashMap<>();
        DataSource masterDataSourceInstance = masterDataSource();
        targetDataSources.put(DataSourceType.MASTER, masterDataSourceInstance);
        targetDataSources.put(DataSourceType.SLAVE, slaveDataSource());
        
        // 设置数据源映射和默认数据源
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(masterDataSourceInstance);
        
        return dynamicDataSource;
    }

    /**
     * 优化HikariCP连接池配置
     * 
     * 功能说明：
     * - 对HikariCP连接池进行性能调优
     * - 设置合适的连接池大小和超时参数
     * - 启用SQL缓存和连接检测功能
     * - 配置MySQL特定的性能优化参数
     * 
     * 业务逻辑：
     * 1. 连接池大小配置：最大20个连接，最小5个空闲连接
     * 2. 超时控制：连接超时30秒，验证超时5秒
     * 3. 连接生命周期管理：空闲10分钟，最大生命周期30分钟
     * 4. 泄露检测：1分钟未关闭的连接会被检测和记录
     * 5. SQL缓存优化：启用预编译语句缓存，提升执行效率
     * 
     * 性能优化参数：
     * - cachePrepStmts: 启用预编译语句缓存
     * - prepStmtCacheSize: 缓存250个预编译语句
     * - prepStmtCacheSqlLimit: 单个语句最大2048字符
     * - useServerPrepStmts: 使用服务器端预编译
     * - rewriteBatchedStatements: 重写批量语句提升性能
     * - cacheResultSetMetadata: 缓存结果集元数据
     * 
     * 注意事项：
     * - 连接池大小需要根据实际并发情况调整
     * - 超时时间需要平衡响应速度和系统稳定性
     * - 生产环境建议根据监控数据进一步优化
     * 
     * @param config HikariCP配置对象
     */
    private void optimizeHikariConfig(HikariConfig config) {
        // 连接池大小配置 - 平衡资源使用和并发能力
        config.setMaximumPoolSize(20);  // 最大连接数，避免数据库连接过多
        config.setMinimumIdle(5);       // 最小空闲连接，保证快速响应
        
        // 超时设置 - 防止连接泄露和长时间阻塞
        config.setConnectionTimeout(30000);  // 30秒连接超时
        config.setValidationTimeout(5000);   // 5秒验证超时  
        config.setIdleTimeout(600000);       // 10分钟空闲超时
        config.setMaxLifetime(1800000);      // 30分钟最大生命周期
        
        // 连接泄露检测 - 帮助发现未正确关闭的连接
        config.setLeakDetectionThreshold(60000); // 1分钟检测阈值
        
        // 连接有效性测试 - 确保连接可用性
        config.setConnectionTestQuery("SELECT 1");
        
        // MySQL性能优化配置 - 提升SQL执行效率
        config.addDataSourceProperty("cachePrepStmts", "true");              // 启用预编译缓存
        config.addDataSourceProperty("prepStmtCacheSize", "250");            // 缓存大小
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");       // 单语句缓存限制
        config.addDataSourceProperty("useServerPrepStmts", "true");          // 服务端预编译
        config.addDataSourceProperty("useLocalSessionState", "true");        // 本地会话状态
        config.addDataSourceProperty("rewriteBatchedStatements", "true");    // 批量语句重写
        config.addDataSourceProperty("cacheResultSetMetadata", "true");      // 结果集元数据缓存
        config.addDataSourceProperty("cacheServerConfiguration", "true");    // 服务器配置缓存
        config.addDataSourceProperty("elideSetAutoCommits", "true");         // 省略自动提交设置
        config.addDataSourceProperty("maintainTimeStats", "false");          // 禁用时间统计
    }
}