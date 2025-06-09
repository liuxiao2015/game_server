package com.game.service.logic.service;

import com.game.common.api.service.IUserService;
import com.game.common.model.entity.User;
import com.game.common.model.exception.ErrorCode;
import com.game.common.model.exception.ServiceException;
import com.game.common.model.request.UserLoginRequest;
import com.game.common.model.response.Result;
import com.game.common.model.response.UserLoginResponse;
import com.game.service.logic.manager.TokenManager;
import com.game.service.logic.manager.UserCache;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户服务实现类
 * 
 * 功能说明：
 * - 实现用户登录认证的完整业务逻辑
 * - 提供用户信息查询、更新等核心数据操作
 * - 管理用户会话和Token生成机制
 * - 支持用户数据的缓存和持久化存储
 * 
 * 技术特点：
 * - 基于Dubbo提供分布式RPC服务
 * - 集成Spring框架的依赖注入和事务管理
 * - 使用内存数据库模拟用户数据存储（演示用途）
 * - 实现用户密码的MD5加密和验证
 * 
 * 服务配置：
 * - Dubbo版本：1.0.0
 * - 服务分组：game
 * - 超时时间：3000ms
 * 
 * 业务流程：
 * 1. 用户登录：验证账号密码 → 生成Token → 返回用户信息
 * 2. 信息查询：根据ID或账号查询用户详细信息
 * 3. 信息更新：更新用户昵称、头像等可修改字段
 * 4. 账号检查：验证账号是否已存在，用于注册验证
 * 
 * 安全措施：
 * - 密码采用MD5加密存储
 * - Token具有时效性和唯一性
 * - 登录日志记录和异常监控
 *
 * @author lx
 * @date 2024-01-01
 */
@DubboService(version = "1.0.0", group = "game", timeout = 3000)
public class UserServiceImpl implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private UserCache userCache;

    // Mock data storage (in real implementation, this would be a database)
    private final ConcurrentHashMap<String, User> userDatabase = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, User> userByIdDatabase = new ConcurrentHashMap<>();
    private final AtomicLong userIdGenerator = new AtomicLong(10000);

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        try {
            logger.info("User login attempt: account={}, deviceId={}", 
                    request.getAccount(), request.getDeviceId());

            // Validate request
            if (request.getAccount() == null || request.getAccount().trim().isEmpty()) {
                return UserLoginResponse.error("Account cannot be empty");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return UserLoginResponse.error("Password cannot be empty");
            }

            // Get or create user
            User user = getOrCreateUser(request.getAccount(), request.getPassword());
            if (user == null) {
                return UserLoginResponse.error("Invalid account or password");
            }

            // Update last login time
            user.setLastLoginTime(System.currentTimeMillis());
            user.setUpdateTime(System.currentTimeMillis());

            // Generate session and token
            String sessionId = UUID.randomUUID().toString();
            String token = tokenManager.generateToken(user.getUserId(), sessionId);

            // Cache user data
            userCache.putUser(user);

            // Create login response
            UserLoginResponse response = UserLoginResponse.success(
                    user.getUserId(), 
                    token, 
                    user.getNickname(), 
                    user.getLevel()
            );
            response.getData().setLastLoginTime(user.getLastLoginTime());
            response.getData().setIsFirstLogin(user.getCreateTime().equals(user.getLastLoginTime()));

            logger.info("User login successful: userId={}, account={}", user.getUserId(), user.getAccount());
            return response;

        } catch (Exception e) {
            logger.error("User login failed: account={}", request.getAccount(), e);
            return UserLoginResponse.error("Login failed: " + e.getMessage());
        }
    }

    @Override
    public Result<User> getUserInfo(Long userId) {
        try {
            if (userId == null) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "User ID cannot be null");
            }

            // Check cache first
            User user = userCache.getUser(userId);
            if (user != null) {
                logger.debug("User found in cache: userId={}", userId);
                return Result.success(user);
            }

            // Get from database
            user = userByIdDatabase.get(userId);
            if (user == null) {
                return Result.failure(ErrorCode.USER_NOT_FOUND, "User not found");
            }

            // Cache the user
            userCache.putUser(user);
            return Result.success(user);

        } catch (Exception e) {
            logger.error("Failed to get user info: userId={}", userId, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to get user info: " + e.getMessage());
        }
    }

    @Override
    public Result<Void> updateUserInfo(Long userId, String nickname, String avatar) {
        try {
            if (userId == null) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "User ID cannot be null");
            }

            User user = userByIdDatabase.get(userId);
            if (user == null) {
                return Result.failure(ErrorCode.USER_NOT_FOUND, "User not found");
            }

            // Update user info
            if (nickname != null && !nickname.trim().isEmpty()) {
                user.setNickname(nickname.trim());
            }
            if (avatar != null && !avatar.trim().isEmpty()) {
                user.setAvatar(avatar.trim());
            }
            user.setUpdateTime(System.currentTimeMillis());

            // Update cache
            userCache.putUser(user);

            logger.info("User info updated: userId={}, nickname={}, avatar={}", userId, nickname, avatar);
            return Result.success();

        } catch (Exception e) {
            logger.error("Failed to update user info: userId={}", userId, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to update user info: " + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> userExists(String account) {
        try {
            if (account == null || account.trim().isEmpty()) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "Account cannot be empty");
            }

            // Check cache first
            if (userCache.containsUserByAccount(account)) {
                return Result.success(true);
            }

            // Check database
            boolean exists = userDatabase.containsKey(account);
            return Result.success(exists);

        } catch (Exception e) {
            logger.error("Failed to check user existence: account={}", account, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to check user existence: " + e.getMessage());
        }
    }

    @Override
    public Result<User> getUserByAccount(String account) {
        try {
            if (account == null || account.trim().isEmpty()) {
                return Result.failure(ErrorCode.PARAMETER_MISSING, "Account cannot be empty");
            }

            // Check cache first
            User user = userCache.getUserByAccount(account);
            if (user != null) {
                return Result.success(user);
            }

            // Get from database
            user = userDatabase.get(account);
            if (user == null) {
                return Result.failure(ErrorCode.USER_NOT_FOUND, "User not found");
            }

            // Cache the user
            userCache.putUser(user);
            return Result.success(user);

        } catch (Exception e) {
            logger.error("Failed to get user by account: account={}", account, e);
            return Result.failure(ErrorCode.SYSTEM_ERROR, "Failed to get user by account: " + e.getMessage());
        }
    }

    /**
     * Get or create user for login
     */
    private User getOrCreateUser(String account, String password) {
        // Get existing user
        User user = userDatabase.get(account);
        
        if (user != null) {
            // Verify password (simple MD5 check for demo)
            String hashedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
            if (!hashedPassword.equals(user.getAvatar())) { // Using avatar field to store password hash for demo
                return null; // Wrong password
            }
            return user;
        }

        // Create new user
        Long userId = userIdGenerator.incrementAndGet();
        user = new User(userId, account, "User" + userId);
        
        // Hash password and store in avatar field for demo
        String hashedPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        user.setAvatar(hashedPassword);
        
        // Store in mock database
        userDatabase.put(account, user);
        userByIdDatabase.put(userId, user);
        
        logger.info("Created new user: userId={}, account={}", userId, account);
        return user;
    }
}