package com.game.adm;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 游戏管理后台应用启动类
 * 
 * 功能说明：
 * - Spring Boot应用的主启动类，负责管理后台系统的启动
 * - 集成Spring Boot Admin Server提供应用监控功能
 * - 支持异步任务处理和定时任务调度
 * - 提供游戏运营和管理的Web界面和API服务
 * 
 * 设计思路：
 * - 采用Spring Boot微服务架构，独立部署和运行
 * - 集成监控和管理功能，便于运维和问题定位
 * - 支持异步处理提升系统性能和响应速度
 * - 提供定时任务支持游戏数据的定期处理
 * 
 * 核心功能：
 * - 应用监控：通过Admin Server监控游戏服务器状态
 * - 数据统计：提供游戏运营数据的统计和分析
 * - GM工具：游戏管理员的操作工具和管理界面
 * - 系统配置：游戏参数和配置的在线管理
 * 
 * 技术特性：
 * - @SpringBootApplication：启用Spring Boot自动配置
 * - @EnableAdminServer：启用Spring Boot Admin监控服务
 * - @EnableScheduling：启用定时任务调度功能
 * - @EnableAsync：启用异步方法执行支持
 * 
 * 使用场景：
 * - 游戏服务器的监控和管理
 * - 运营数据的统计和报表生成
 * - 游戏配置的动态调整和更新
 * - 系统性能的监控和优化
 *
 * @author lx
 * @date 2025/06/08
 */
@SpringBootApplication
@EnableAdminServer
@EnableScheduling
@EnableAsync
public class AdmApplication {
    
    /**
     * 应用启动入口方法
     * 
     * 功能说明：
     * - Java应用程序的主入口点
     * - 启动Spring Boot应用容器和管理后台服务
     * - 初始化所有相关的Bean和服务组件
     * 
     * 启动过程：
     * 1. 解析命令行参数和配置文件
     * 2. 初始化Spring应用上下文
     * 3. 启动内嵌的Web服务器
     * 4. 加载并启动所有配置的服务
     * 5. 启动Admin Server监控服务
     * 
     * @param args 命令行启动参数，可用于传递配置选项
     */
    public static void main(String[] args) {
        SpringApplication.run(AdmApplication.class, args);
    }
}