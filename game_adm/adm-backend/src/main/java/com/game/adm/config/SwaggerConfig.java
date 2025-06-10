package com.game.adm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API文档配置类
 * 
 * 功能说明：
 * - 配置游戏管理后台的API文档生成和展示
 * - 基于OpenAPI 3.0规范，提供标准化的接口文档
 * - 支持在线接口测试和调试功能
 * - 便于前端开发和第三方系统集成
 * 
 * 设计思路：
 * - 使用Spring Boot与Swagger集成，自动扫描Controller生成文档
 * - 配置详细的API信息，包括版本、描述、联系方式等
 * - 提供清晰的接口分组和标签管理
 * - 支持多环境配置，开发、测试、生产环境可分别配置
 * 
 * 使用场景：
 * - 管理后台接口文档：GM工具、数据统计、系统监控等接口
 * - 前端开发辅助：提供完整的接口规范和示例
 * - 接口测试工具：支持在线调试和参数验证
 * - 第三方集成：为外部系统提供标准化的接口文档
 * 
 * 技术特点：
 * - OpenAPI 3.0：使用最新的API规范标准
 * - 自动生成：基于注解自动生成接口文档
 * - 交互式文档：支持在线测试和参数填写
 * - 多格式导出：支持JSON、YAML等格式导出
 * 
 * 访问方式：
 * - 文档地址：http://localhost:port/swagger-ui.html
 * - API规范：http://localhost:port/v3/api-docs
 * - 支持多种UI主题和自定义样式
 * 
 * @author lx
 * @date 2025/06/08
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * 配置游戏服务器管理后台的OpenAPI文档
     * 
     * 功能说明：
     * - 创建OpenAPI文档配置对象，定义API文档的基本信息
     * - 设置API标题、版本、描述等元数据信息
     * - 配置开发团队联系方式，便于沟通和技术支持
     * 
     * 文档配置：
     * 1. 标题：游戏服务器管理后台API - 明确标识API用途
     * 2. 版本：1.0.0 - 遵循语义化版本控制
     * 3. 描述：详细说明API功能范围和使用场景
     * 4. 联系信息：开发团队信息，便于问题反馈和技术咨询
     * 
     * 文档内容：
     * - 统计数据接口：服务器运行状态、用户数据统计
     * - GM工具接口：游戏管理、用户管理、道具发放等
     * - 系统监控接口：性能监控、日志查询、异常报警
     * - 配置管理接口：游戏配置、服务器配置的查询和修改
     * 
     * 使用价值：
     * - 提升开发效率：标准化的接口文档减少沟通成本
     * - 便于测试：在线接口测试功能，快速验证API行为
     * - 促进协作：前后端分离开发的重要桥梁
     * - 文档维护：代码即文档，保证文档与实现同步
     * 
     * @return OpenAPI 配置完整的OpenAPI文档对象
     */
    @Bean
    public OpenAPI gameServerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("游戏服务器管理后台API")                    // 设置API文档标题
                .version("1.0.0")                               // 设置API版本号
                .description("游戏服务器管理后台接口文档，包含统计数据、GM工具等功能") // 详细描述
                .contact(new Contact()                          // 配置联系信息
                    .name("Game Development Team")             // 开发团队名称
                    .email("dev@game.com")                     // 联系邮箱
                    .url("https://game.com")                   // 官方网站
                )
            );
    }
}