package com.game.adm;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 管理后台启动类
 * @author lx
 * @date 2025/06/08
 */
@SpringBootApplication
@EnableAdminServer
@EnableScheduling
@EnableAsync
public class AdmApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdmApplication.class, args);
    }
}