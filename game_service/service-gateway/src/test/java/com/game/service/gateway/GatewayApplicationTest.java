package com.game.service.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for Gateway Application
 *
 * @author lx
 * @date 2024-01-01
 */
@SpringBootTest
@TestPropertySource(properties = {
    "gateway.port=8889"  // Use different port for testing
})
public class GatewayApplicationTest {
    
    @Test
    public void contextLoads() {
        // Test that Spring context loads successfully
    }
}