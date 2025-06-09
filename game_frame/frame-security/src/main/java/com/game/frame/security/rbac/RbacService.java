package com.game.frame.security.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * RBAC服务
 * @author lx
 * @date 2025/06/08
 */
@Service
public class RbacService {
    private static final Logger logger = LoggerFactory.getLogger(RbacService.class);
    
    private static final String PERMISSION_CACHE_PREFIX = "security:permission:";
    private static final String ROLE_CACHE_PREFIX = "security:role:";
    private static final String USER_ROLE_PREFIX = "security:user:role:";
    
    // In-memory cache for frequently accessed permissions
    private final Map<String, Permission> permissionCache = new ConcurrentHashMap<>();
    private final Map<String, Role> roleCache = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 权限检查
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        try {
            // Get user roles
            Set<String> userRoles = getUserRoles(userId);
            if (userRoles.isEmpty()) {
                return false;
            }

            // Check if any role has the required permission
            for (String roleCode : userRoles) {
                Role role = getRole(roleCode);
                if (role != null && role.hasPermission(permissionCode)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Error checking permission for user {}, permission {}", userId, permissionCode, e);
            return false;
        }
    }

    /**
     * 检查多个权限
     */
    public boolean hasPermissions(Long userId, String[] permissionCodes, RequirePermission.Logical logical) {
        if (permissionCodes == null || permissionCodes.length == 0) {
            return true;
        }

        boolean result = logical == RequirePermission.Logical.AND;
        
        for (String permissionCode : permissionCodes) {
            boolean hasPermission = hasPermission(userId, permissionCode);
            
            if (logical == RequirePermission.Logical.AND) {
                result = result && hasPermission;
                if (!result) break; // Short circuit for AND
            } else {
                result = result || hasPermission;
                if (result) break; // Short circuit for OR
            }
        }
        
        return result;
    }

    /**
     * 角色分配
     */
    public void assignRole(Long userId, String roleCode) {
        try {
            String key = USER_ROLE_PREFIX + userId;
            redisTemplate.opsForSet().add(key, roleCode);
            redisTemplate.expire(key, 24, TimeUnit.HOURS); // Cache for 24 hours
            logger.info("Assigned role {} to user {}", roleCode, userId);
        } catch (Exception e) {
            logger.error("Failed to assign role {} to user {}", roleCode, userId, e);
        }
    }

    /**
     * 移除角色
     */
    public void removeRole(Long userId, String roleCode) {
        try {
            String key = USER_ROLE_PREFIX + userId;
            redisTemplate.opsForSet().remove(key, roleCode);
            logger.info("Removed role {} from user {}", roleCode, userId);
        } catch (Exception e) {
            logger.error("Failed to remove role {} from user {}", roleCode, userId, e);
        }
    }

    /**
     * 获取用户角色
     */
    @SuppressWarnings("unchecked")
    /**
     * 获取用户的所有权限
     */
    public Set<String> getUserPermissions(Long userId) {
        try {
            Set<String> userRoles = getUserRoles(userId);
            Set<String> permissions = new HashSet<>();
            
            for (String roleCode : userRoles) {
                Role role = getRole(roleCode);
                if (role != null && role.getPermissions() != null) {
                    // Extract permission codes from Permission objects
                    for (Permission permission : role.getPermissions()) {
                        permissions.add(permission.getCode());
                    }
                }
            }
            
            return permissions;
        } catch (Exception e) {
            logger.error("Failed to get permissions for user {}", userId, e);
            return new HashSet<>();
        }
    }

    public Set<String> getUserRoles(Long userId) {
        try {
            String key = USER_ROLE_PREFIX + userId;
            Set<Object> roles = redisTemplate.opsForSet().members(key);
            
            Set<String> result = new HashSet<>();
            if (roles != null) {
                for (Object role : roles) {
                    result.add(role.toString());
                }
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Failed to get roles for user {}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 动态权限加载
     */
    public void loadPermission(Permission permission) {
        try {
            permissionCache.put(permission.getCode(), permission);
            
            String key = PERMISSION_CACHE_PREFIX + permission.getCode();
            redisTemplate.opsForValue().set(key, permission, 1, TimeUnit.HOURS);
            
            logger.info("Loaded permission: {}", permission.getCode());
        } catch (Exception e) {
            logger.error("Failed to load permission: {}", permission.getCode(), e);
        }
    }

    /**
     * 动态角色加载
     */
    public void loadRole(Role role) {
        try {
            roleCache.put(role.getCode(), role);
            
            String key = ROLE_CACHE_PREFIX + role.getCode();
            redisTemplate.opsForValue().set(key, role, 1, TimeUnit.HOURS);
            
            logger.info("Loaded role: {}", role.getCode());
        } catch (Exception e) {
            logger.error("Failed to load role: {}", role.getCode(), e);
        }
    }

    /**
     * 获取权限
     */
    public Permission getPermission(String permissionCode) {
        // Check memory cache first
        Permission permission = permissionCache.get(permissionCode);
        if (permission != null) {
            return permission;
        }

        // Check Redis cache
        try {
            String key = PERMISSION_CACHE_PREFIX + permissionCode;
            permission = (Permission) redisTemplate.opsForValue().get(key);
            if (permission != null) {
                permissionCache.put(permissionCode, permission); // Update memory cache
                return permission;
            }
        } catch (Exception e) {
            logger.error("Failed to get permission from cache: {}", permissionCode, e);
        }

        return null;
    }

    /**
     * 获取角色
     */
    public Role getRole(String roleCode) {
        // Check memory cache first
        Role role = roleCache.get(roleCode);
        if (role != null) {
            return role;
        }

        // Check Redis cache
        try {
            String key = ROLE_CACHE_PREFIX + roleCode;
            role = (Role) redisTemplate.opsForValue().get(key);
            if (role != null) {
                roleCache.put(roleCode, role); // Update memory cache
                return role;
            }
        } catch (Exception e) {
            logger.error("Failed to get role from cache: {}", roleCode, e);
        }

        return null;
    }

    /**
     * 清除权限缓存
     */
    public void clearPermissionCache() {
        permissionCache.clear();
        logger.info("Permission cache cleared");
    }

    /**
     * 清除角色缓存
     */
    public void clearRoleCache() {
        roleCache.clear();
        logger.info("Role cache cleared");
    }

    /**
     * 初始化默认权限和角色
     */
    public void initializeDefaultRoles() {
        // Create default permissions
        Permission userView = new Permission("user.view", "查看用户", "user", "read");
        Permission userEdit = new Permission("user.edit", "编辑用户", "user", "write");
        Permission adminAccess = new Permission("admin.access", "管理员访问", "admin", "access");
        
        loadPermission(userView);
        loadPermission(userEdit);
        loadPermission(adminAccess);

        // Create default roles
        Role userRole = new Role("USER", "普通用户");
        userRole.addPermission(userView);
        
        Role adminRole = new Role("ADMIN", "管理员");
        adminRole.addPermission(userView);
        adminRole.addPermission(userEdit);
        adminRole.addPermission(adminAccess);
        
        loadRole(userRole);
        loadRole(adminRole);
        
        logger.info("Default roles and permissions initialized");
    }
}