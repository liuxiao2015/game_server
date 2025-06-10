package com.game.frame.data.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA数据访问层配置类
 * 
 * 功能说明：
 * - 配置Spring Data JPA的核心功能，简化数据库访问层开发
 * - 启用JPA审计功能，自动管理创建时间、修改时间等字段
 * - 扫描和注册实体类和Repository接口，实现自动化配置
 * - 集成事务管理，确保数据操作的ACID特性
 * 
 * 设计思路：
 * - 采用注解驱动的配置方式，减少XML配置的复杂性
 * - 实体和Repository分离扫描，提供灵活的包结构管理
 * - 审计功能自动化，减少手动维护审计字段的工作量
 * - 事务管理声明式配置，支持注解和编程式事务
 * 
 * 使用场景：
 * - 游戏用户数据的持久化操作，包括用户信息、游戏进度等
 * - 游戏配置数据的查询和管理，支持复杂的关联查询
 * - 审计日志的自动记录，追踪数据变更历史
 * - 事务性业务操作，确保数据一致性和完整性
 * 
 * 技术特点：
 * - Spring Data JPA自动Repository实现
 * - 支持JPQL和原生SQL查询
 * - 分页和排序功能内置支持
 * - 延迟加载和缓存优化
 * 
 * 包扫描策略：
 * - Repository扫描：frame-data和common模块的Repository接口
 * - Entity扫描：frame-data和common模块的实体类
 * - 支持多模块的实体和Repository共享
 * 
 * @author lx
 * @date 2025/06/08
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
    "com.game.frame.data.repository",
    "com.game.common.entity"
})
@EntityScan(basePackages = {
    "com.game.frame.data.entity",
    "com.game.common.entity"
})
public class JpaConfig {
    
    /**
     * JPA配置说明
     * 
     * 配置方式：
     * - 主要的JPA配置通过application.yml文件进行，包括：
     *   * spring.jpa.hibernate.ddl-auto: 数据库schema更新策略
     *   * spring.jpa.show-sql: 是否显示SQL语句
     *   * spring.jpa.database-platform: 数据库方言配置
     *   * spring.jpa.properties.hibernate.*: Hibernate特定配置
     * 
     * 注解功能：
     * - @EnableJpaAuditing: 启用审计功能，自动填充@CreatedDate、@LastModifiedDate等字段
     * - @EnableTransactionManagement: 启用声明式事务管理，支持@Transactional注解
     * - @EnableJpaRepositories: 扫描并自动实现Repository接口
     * - @EntityScan: 扫描JPA实体类，支持多包扫描
     * 
     * 审计功能：
     * - 自动记录实体的创建时间和修改时间
     * - 支持创建人和修改人的记录(需要配置AuditorAware)
     * - 减少手动维护审计字段的代码量
     * 
     * Repository自动实现：
     * - 基于接口自动生成实现类
     * - 支持方法名查询、@Query注解查询
     * - 内置分页、排序、批量操作功能
     */
}