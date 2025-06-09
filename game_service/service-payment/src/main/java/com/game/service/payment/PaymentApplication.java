package com.game.service.payment;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Payment service application
 * Provides payment processing with multiple channels and virtual currency support
 *
 * @author lx
 * @date 2025/01/08
 */
@SpringBootApplication
@EnableDubbo
@EnableAsync
@EnableScheduling
public class PaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}