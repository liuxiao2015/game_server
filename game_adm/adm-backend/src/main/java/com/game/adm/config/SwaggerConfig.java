package com.game.adm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置
 * @author lx
 * @date 2025/06/08
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI gameServerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("游戏服务器管理后台API")
                .version("1.0.0")
                .description("游戏服务器管理后台接口文档，包含统计数据、GM工具等功能")
                .contact(new Contact()
                    .name("Game Development Team")
                    .email("dev@game.com")
                    .url("https://game.com")
                )
            );
    }
}