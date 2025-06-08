package com.game.common.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户实体测试
 * @author lx
 * @date 2025/06/08
 */
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
class UserEntityTest {

    @Test
    void testUserEntityCreation() {
        UserEntity user = new UserEntity();
        user.setUsername("test_user");
        user.setNickname("Test User");
        user.setLevel(10);
        user.setExp(5000L);
        user.setVipLevel(1);
        user.setLastLoginTime(LocalDateTime.now());

        // 验证属性设置
        assertEquals("test_user", user.getUsername());
        assertEquals("Test User", user.getNickname());
        assertEquals(Integer.valueOf(10), user.getLevel());
        assertEquals(Long.valueOf(5000L), user.getExp());
        assertEquals(Integer.valueOf(1), user.getVipLevel());
        assertNotNull(user.getLastLoginTime());
    }

    @Test
    void testDefaultValues() {
        UserEntity user = new UserEntity();
        
        // 验证默认值
        assertEquals(Integer.valueOf(1), user.getLevel());
        assertEquals(Long.valueOf(0L), user.getExp());
        assertEquals(Integer.valueOf(0), user.getVipLevel());
        assertEquals(Integer.valueOf(0), user.getDeleted());
    }

    @Test
    void testEqualsAndHashCode() {
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        user1.setUsername("test_user");

        UserEntity user2 = new UserEntity();
        user2.setId(1L);
        user2.setUsername("test_user");

        UserEntity user3 = new UserEntity();
        user3.setId(2L);
        user3.setUsername("another_user");

        // 测试equals
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);

        // 测试hashCode
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void testToString() {
        UserEntity user = new UserEntity();
        user.setUsername("test_user");
        user.setNickname("Test User");
        user.setLevel(10);

        String toString = user.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test_user"));
        assertTrue(toString.contains("Test User"));
        assertTrue(toString.contains("10"));
    }
}