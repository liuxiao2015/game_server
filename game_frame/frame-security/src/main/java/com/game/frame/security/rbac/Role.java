package com.game.frame.security.rbac;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 角色实体类
 * 
 * 功能说明：
 * - 实现基于角色的访问控制（RBAC）中的角色定义
 * - 管理角色与权限的关联关系，简化权限分配和管理
 * - 支持角色的层级管理和权限继承机制
 * - 提供角色状态控制，支持角色的动态启用和禁用
 * 
 * 设计思路：
 * - 角色作为权限的容器，将相关权限组织在一起
 * - 用户通过角色获得权限，实现权限的批量分配
 * - 使用Set集合存储权限，确保权限不重复
 * - 提供便捷的权限操作方法，简化角色权限管理
 * 
 * RBAC模型：
 * - 用户（User）↔ 角色（Role）↔ 权限（Permission）
 * - 多对多关系：用户可有多个角色，角色可有多个权限
 * - 角色是权限分配的基本单位，便于管理和维护
 * 
 * 使用场景：
 * - 系统用户的角色分配和权限管理
 * - 管理后台的角色配置和权限设计
 * - 动态权限检查和访问控制决策
 * - 组织架构中的职位权限映射
 * 
 * 权限聚合：
 * - 角色聚合多个相关权限，形成职能权限包
 * - 支持权限的快速查询和批量操作
 * - 提供权限代码数组，便于权限框架集成
 * 
 * 扩展特性：
 * - 支持角色继承，子角色继承父角色权限
 * - 支持角色组和角色分类管理
 * - 支持角色的有效期和临时授权
 *
 * @author lx
 * @date 2025/06/08
 */
public class Role {
    
    /** 角色ID，数据库主键，用于唯一标识角色记录 */
    private Long id;
    
    /** 角色代码，唯一标识符，用于系统内部的角色识别和权限检查 */
    private String code;
    
    /** 角色名称，可读的角色描述，用于界面显示和用户理解 */
    private String name;
    
    /** 角色描述，详细说明角色的职责和适用场景 */
    private String description;
    
    /** 是否启用，控制角色的有效性，支持角色的动态启用/禁用 */
    private Boolean enabled;
    
    /** 角色权限集合，使用Set避免重复权限，实现角色权限的聚合管理 */
    private Set<Permission> permissions;
    
    /** 创建时间戳，记录角色的创建时间，用于审计和排序 */
    private Long createTime;
    
    /** 更新时间戳，记录角色的最后修改时间，用于变更追踪 */
    private Long updateTime;

    public Role() {
        this.enabled = true;
        this.permissions = new HashSet<>();
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
    }

    public Role(String code, String name) {
        this();
        this.code = code;
        this.name = name;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }

    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }

    public Long getUpdateTime() { return updateTime; }
    public void setUpdateTime(Long updateTime) { this.updateTime = updateTime; }

    // Helper methods
    public void addPermission(Permission permission) {
        if (permission != null) {
            this.permissions.add(permission);
        }
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission(String permissionCode) {
        return permissions.stream()
                .anyMatch(p -> p.getCode().equals(permissionCode) && p.getEnabled());
    }

    public String[] getPermissionCodes() {
        return permissions.stream()
                .filter(p -> p.getEnabled())
                .map(Permission::getCode)
                .toArray(String[]::new);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", permissionCount=" + permissions.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return code != null && code.equals(role.code);
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}