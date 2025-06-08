package com.game.common.api.service;

import com.game.common.model.request.UserLoginRequest;
import com.game.common.model.response.UserLoginResponse;
import com.game.common.model.response.Result;

/**
 * User service interface
 * Provides user login, information retrieval and update operations
 *
 * @author lx
 * @date 2024-01-01
 */
public interface IUserService {

    /**
     * User login
     *
     * @param request login request
     * @return login response with user info and token
     */
    UserLoginResponse login(UserLoginRequest request);

    /**
     * Get user information by user ID
     *
     * @param userId user ID
     * @return user information
     */
    Result<com.game.common.model.entity.User> getUserInfo(Long userId);

    /**
     * Update user information
     *
     * @param userId user ID
     * @param nickname new nickname
     * @param avatar new avatar
     * @return update result
     */
    Result<Void> updateUserInfo(Long userId, String nickname, String avatar);

    /**
     * Check if user exists
     *
     * @param account user account
     * @return true if exists
     */
    Result<Boolean> userExists(String account);

    /**
     * Get user by account
     *
     * @param account user account
     * @return user entity
     */
    Result<com.game.common.model.entity.User> getUserByAccount(String account);
}