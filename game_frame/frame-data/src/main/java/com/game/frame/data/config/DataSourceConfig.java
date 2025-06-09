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
 * 数据源配置
 * @author lx
 * @date 2025/06/08
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    /**
     * 主数据源配置
     * @return HikariConfig
     */
    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public HikariConfig masterDataSourceConfig() {
        return new HikariConfig();
    }

    /**
     * 从数据源配置
     * @return HikariConfig
     */
    @Bean
    @ConfigurationProperties("spring.datasource.slave")
    public HikariConfig slaveDataSourceConfig() {
        return new HikariConfig();
    }

    /**
     * 主数据源
     * @return DataSource
     */
    @Bean
    public DataSource masterDataSource() {
        HikariConfig config = masterDataSourceConfig();
        optimizeHikariConfig(config);
        return new HikariDataSource(config);
    }

    /**
     * 从数据源
     * @return DataSource
     */
    @Bean
    public DataSource slaveDataSource() {
        HikariConfig config = slaveDataSourceConfig();
        optimizeHikariConfig(config);
        return new HikariDataSource(config);
    }

    /**
     * 动态数据源
     * @return DataSource
     */
    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        DataSource masterDataSourceInstance = masterDataSource();
        targetDataSources.put(DataSourceType.MASTER, masterDataSourceInstance);
        targetDataSources.put(DataSourceType.SLAVE, slaveDataSource());
        
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(masterDataSourceInstance);
        
        return dynamicDataSource;
    }

    /**
     * 优化HikariCP配置
     * @param config HikariConfig
     */
    private void optimizeHikariConfig(HikariConfig config) {
        // 连接池大小
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        
        // 超时设置
        config.setConnectionTimeout(30000); // 30秒
        config.setValidationTimeout(5000);  // 5秒
        config.setIdleTimeout(600000);      // 10分钟
        config.setMaxLifetime(1800000);     // 30分钟
        
        // 连接泄露检测
        config.setLeakDetectionThreshold(60000); // 1分钟
        
        // 连接测试
        config.setConnectionTestQuery("SELECT 1");
        
        // 缓存配置
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
    }
}