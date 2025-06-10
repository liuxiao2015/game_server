package com.game.frame.data.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB数据库配置类
 * 
 * 功能说明：
 * - 配置MongoDB数据库连接和基础设置，支持游戏日志数据的持久化存储
 * - 启用MongoDB仓储层自动配置，简化数据访问层的开发和维护
 * - 提供自动索引创建功能，优化查询性能和数据检索效率
 * - 集成Spring Data MongoDB，支持响应式编程和传统的阻塞式操作
 * 
 * 设计思路：
 * - 继承AbstractMongoClientConfiguration实现标准化的MongoDB配置
 * - 使用@EnableMongoRepositories注解启用仓储层自动扫描和注册
 * - 采用约定大于配置的原则，减少配置复杂度
 * - 支持多数据源配置和分库分表的扩展需求
 * 
 * 使用场景：
 * - 游戏操作日志的实时记录和查询
 * - 用户行为数据的统计分析和报表生成
 * - 游戏事件的审计追踪和合规性检查
 * - 大数据量的日志存储和历史数据归档
 * 
 * 技术特点：
 * - 自动索引创建，提升查询性能
 * - 支持文档型数据存储，适合复杂数据结构
 * - 高可用和分布式部署能力
 * - 与Spring生态无缝集成
 * 
 * @author lx
 * @date 2025/06/08
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.game.frame.data.mongo")
public class MongoConfig extends AbstractMongoClientConfiguration {

    /**
     * 获取数据库名称
     * 
     * 业务逻辑：
     * - 返回游戏日志数据库的名称
     * - 用于存储游戏运行过程中产生的各类日志数据
     * - 包括用户操作日志、系统事件日志、错误日志等
     * 
     * @return 数据库名称 "game_logs"
     */
    @Override
    protected String getDatabaseName() {
        return "game_logs";
    }

    /**
     * 配置自动索引创建
     * 
     * 业务逻辑：
     * - 启用自动索引创建功能，MongoDB会根据查询模式自动创建合适的索引
     * - 提升查询性能，特别是对于频繁查询的字段
     * - 减少手动索引管理的复杂度，提高开发效率
     * 
     * 性能考虑：
     * - 自动索引可能会在初始阶段增加写入延迟
     * - 建议在生产环境中根据实际查询需求手动优化索引
     * 
     * @return true 启用自动索引创建
     */
    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}