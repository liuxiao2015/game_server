package com.game.frame.security.rbac;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 角色实体
 * @author lx
 * @date 2025/06/08
 */
public class Role {
    private Long id;
    private String code;        // 角色代码
    private String name;        // 角色名称
    private String description; // 角色描述
    private Boolean enabled;    // 是否启用
    private Set<Permission> permissions; // 角色权限
    private Long createTime;
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