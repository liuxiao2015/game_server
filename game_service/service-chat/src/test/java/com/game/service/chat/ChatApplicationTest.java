package com.game.service.chat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Chat service integration test
 * Tests the basic startup and configuration of chat service
 *
 * @author lx
 * @date 2025/01/08
 */
@SpringBootTest
@ActiveProfiles("test")
class ChatApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // Additional integration tests can be added here
    }
}