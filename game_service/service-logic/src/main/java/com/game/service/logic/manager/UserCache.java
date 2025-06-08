package com.game.service.logic.manager;

import com.game.common.model.entity.User;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * User cache manager using Caffeine
 * Provides local caching with expiration policy for user data
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
public class UserCache {

    private static final Logger logger = LoggerFactory.getLogger(UserCache.class);

    @Value("${game.cache.user.expire:300}")
    private int userCacheExpireSeconds;

    @Value("${game.cache.user.maxSize:10000}")
    private int userCacheMaxSize;

    private Cache<Long, User> userCache;
    private Cache<String, User> accountUserCache;

    @PostConstruct
    public void init() {
        userCache = Caffeine.newBuilder()
                .maximumSize(userCacheMaxSize)
                .expireAfterWrite(userCacheExpireSeconds, TimeUnit.SECONDS)
                .recordStats()
                .build();

        accountUserCache = Caffeine.newBuilder()
                .maximumSize(userCacheMaxSize)
                .expireAfterWrite(userCacheExpireSeconds, TimeUnit.SECONDS)
                .recordStats()
                .build();

        logger.info("UserCache initialized with expireSeconds={}, maxSize={}", 
                userCacheExpireSeconds, userCacheMaxSize);
    }

    /**
     * Get user by user ID
     *
     * @param userId user ID
     * @return user entity or null if not cached
     */
    public User getUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userCache.getIfPresent(userId);
    }

    /**
     * Get user by account
     *
     * @param account user account
     * @return user entity or null if not cached
     */
    public User getUserByAccount(String account) {
        if (account == null || account.trim().isEmpty()) {
            return null;
        }
        return accountUserCache.getIfPresent(account);
    }

    /**
     * Put user into cache
     *
     * @param user user entity
     */
    public void putUser(User user) {
        if (user != null && user.getUserId() != null) {
            userCache.put(user.getUserId(), user);
            if (user.getAccount() != null) {
                accountUserCache.put(user.getAccount(), user);
            }
            logger.debug("Cached user: userId={}, account={}", user.getUserId(), user.getAccount());
        }
    }

    /**
     * Remove user from cache
     *
     * @param userId user ID
     */
    public void removeUser(Long userId) {
        if (userId != null) {
            User user = userCache.getIfPresent(userId);
            userCache.invalidate(userId);
            if (user != null && user.getAccount() != null) {
                accountUserCache.invalidate(user.getAccount());
            }
            logger.debug("Removed user from cache: userId={}", userId);
        }
    }

    /**
     * Remove user from cache by account
     *
     * @param account user account
     */
    public void removeUserByAccount(String account) {
        if (account != null && !account.trim().isEmpty()) {
            User user = accountUserCache.getIfPresent(account);
            accountUserCache.invalidate(account);
            if (user != null && user.getUserId() != null) {
                userCache.invalidate(user.getUserId());
            }
            logger.debug("Removed user from cache: account={}", account);
        }
    }

    /**
     * Check if user exists in cache
     *
     * @param userId user ID
     * @return true if exists in cache
     */
    public boolean containsUser(Long userId) {
        return userId != null && userCache.getIfPresent(userId) != null;
    }

    /**
     * Check if user exists in cache by account
     *
     * @param account user account
     * @return true if exists in cache
     */
    public boolean containsUserByAccount(String account) {
        return account != null && !account.trim().isEmpty() && 
               accountUserCache.getIfPresent(account) != null;
    }

    /**
     * Clear all cached users
     */
    public void clearAll() {
        long userCount = userCache.estimatedSize();
        long accountCount = accountUserCache.estimatedSize();
        userCache.invalidateAll();
        accountUserCache.invalidateAll();
        logger.info("Cleared all user cache: userCount={}, accountCount={}", userCount, accountCount);
    }

    /**
     * Get cache statistics
     *
     * @return cache stats as string
     */
    public String getCacheStats() {
        return String.format("UserCache Stats - Users: %s, Accounts: %s", 
                userCache.stats(), accountUserCache.stats());
    }

    /**
     * Get estimated cache size
     *
     * @return estimated cache size
     */
    public long getEstimatedSize() {
        return userCache.estimatedSize();
    }
}