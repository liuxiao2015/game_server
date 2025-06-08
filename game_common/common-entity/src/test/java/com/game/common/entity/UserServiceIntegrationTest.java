package com.game.common.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务集成测试
 * @author lx
 * @date 2025/06/08
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.master.jdbc-url=jdbc:h2:mem:testdb",
    "spring.datasource.master.driver-class-name=org.h2.Driver",
    "spring.datasource.slave.jdbc-url=jdbc:h2:mem:testdb",
    "spring.datasource.slave.driver-class-name=org.h2.Driver",
    "spring.redis.host=localhost",
    "spring.redis.port=6379"
})
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateAndFindUser() {
        // 创建用户
        UserEntity user = new UserEntity();
        user.setUsername("test_user");
        user.setNickname("Test User");
        user.setLevel(10);
        user.setExp(5000L);
        user.setVipLevel(1);
        user.setLastLoginTime(LocalDateTime.now());

        UserEntity savedUser = userService.createUser(user);
        assertNotNull(savedUser.getId());

        // 根据ID查找
        Optional<UserEntity> foundById = userService.findById(savedUser.getId());
        assertTrue(foundById.isPresent());
        assertEquals("test_user", foundById.get().getUsername());

        // 根据用户名查找
        Optional<UserEntity> foundByUsername = userService.findByUsername("test_user");
        assertTrue(foundByUsername.isPresent());
        assertEquals(savedUser.getId(), foundByUsername.get().getId());
    }

    @Test
    void testUpdateUser() {
        // 创建用户
        UserEntity user = new UserEntity();
        user.setUsername("update_test");
        user.setLevel(1);
        user.setExp(0L);

        UserEntity savedUser = userService.createUser(user);

        // 更新用户
        savedUser.setLevel(20);
        savedUser.setExp(10000L);
        savedUser.setNickname("Updated User");

        UserEntity updatedUser = userService.updateUser(savedUser);
        assertEquals(Integer.valueOf(20), updatedUser.getLevel());
        assertEquals(Long.valueOf(10000L), updatedUser.getExp());
        assertEquals("Updated User", updatedUser.getNickname());
    }

    @Test
    void testUpdateLastLoginTime() {
        // 创建用户
        UserEntity user = new UserEntity();
        user.setUsername("login_test");
        UserEntity savedUser = userService.createUser(user);

        // 更新最后登录时间
        boolean result = userService.updateLastLoginTime(savedUser.getId());
        assertTrue(result);

        // 验证更新
        Optional<UserEntity> foundUser = userService.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertNotNull(foundUser.get().getLastLoginTime());
    }

    @Test
    void testBatchUpdateLevel() {
        // 创建多个用户
        UserEntity user1 = new UserEntity();
        user1.setUsername("batch_test1");
        user1.setLevel(1);

        UserEntity user2 = new UserEntity();
        user2.setUsername("batch_test2");
        user2.setLevel(1);

        UserEntity savedUser1 = userService.createUser(user1);
        UserEntity savedUser2 = userService.createUser(user2);

        // 批量更新等级
        List<Long> userIds = Arrays.asList(savedUser1.getId(), savedUser2.getId());
        int affected = userService.batchUpdateLevel(userIds, 50);
        assertEquals(2, affected);
    }

    @Test
    void testCountUsers() {
        // 创建一些用户
        for (int i = 0; i < 5; i++) {
            UserEntity user = new UserEntity();
            user.setUsername("count_test" + i);
            user.setLevel(i + 1);
            userService.createUser(user);
        }

        // 统计总数
        long totalCount = userService.countActiveUsers();
        assertTrue(totalCount >= 5);

        // 统计指定等级
        long levelCount = userService.countByLevel(3);
        assertTrue(levelCount >= 1);
    }

    @Test
    void testFindByLevelRange() {
        // 创建不同等级的用户
        for (int level = 1; level <= 10; level++) {
            UserEntity user = new UserEntity();
            user.setUsername("level_test" + level);
            user.setLevel(level);
            userService.createUser(user);
        }

        // 查找等级范围
        List<UserEntity> users = userService.findByLevelRange(5, 8);
        assertFalse(users.isEmpty());
        
        // 验证等级范围
        for (UserEntity user : users) {
            assertTrue(user.getLevel() >= 5 && user.getLevel() <= 8);
        }
    }

    @Test
    void testDeleteUser() {
        // 创建用户
        UserEntity user = new UserEntity();
        user.setUsername("delete_test");
        UserEntity savedUser = userService.createUser(user);

        // 删除用户（逻辑删除）
        boolean result = userService.deleteUser(savedUser.getId());
        assertTrue(result);

        // 验证用户已被标记为删除
        Optional<UserEntity> foundUser = userService.findById(savedUser.getId());
        // 由于是逻辑删除，用户仍然存在但被标记为已删除
        assertTrue(foundUser.isPresent());
        assertEquals(Integer.valueOf(1), foundUser.get().getDeleted());
    }
}