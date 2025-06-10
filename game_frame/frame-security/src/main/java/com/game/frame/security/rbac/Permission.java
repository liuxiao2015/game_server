package com.game.frame.security.rbac;

/**
 * 权限实体类
 * 
 * 功能说明：
 * - 定义系统中的具体权限项，实现细粒度的权限控制
 * - 支持基于资源和操作的权限模型，提供灵活的权限管理
 * - 与角色系统配合，实现RBAC（基于角色的访问控制）
 * - 支持权限的动态启用/禁用，便于权限策略调整
 * 
 * 设计思路：
 * - 采用"资源+操作"的权限模型，精确控制用户行为
 * - 使用权限代码作为唯一标识，便于代码中的权限检查
 * - 提供可读的权限名称和描述，便于管理界面展示
 * - 支持权限的启用状态管理，实现权限的动态控制
 * 
 * 权限模型：
 * - code：权限的唯一标识符，格式如"user:read"、"order:delete"
 * - resource：权限涉及的资源，如"user"、"order"、"product"
 * - action：在资源上的操作，如"read"、"write"、"delete"
 * - name：权限的显示名称，如"用户查看"、"订单删除"
 * 
 * 使用场景：
 * - Spring Security中的权限验证和授权决策
 * - 管理后台的权限配置和角色分配
 * - API接口的权限拦截和访问控制
 * - 前端菜单和按钮的权限显示控制
 * 
 * 权限检查：
 * - 基于权限代码进行快速匹配和验证
 * - 支持通配符权限，如"user:*"表示用户模块所有权限
 * - 支持层级权限，如"system:admin:*"表示系统管理权限
 * 
 * 数据管理：
 * - 支持权限的创建、修改、删除和查询
 * - 记录权限的创建和更新时间，便于审计
 * - 支持权限的批量导入和导出
 * 
 * 扩展特性：
 * - 支持权限组和权限分类管理
 * - 支持权限的依赖关系定义
 * - 支持临时权限和权限过期机制
 *
 * @author lx
 * @date 2025/06/08
 */
public class Permission {
    
    /** 权限ID，数据库主键，用于唯一标识权限记录 */
    private Long id;
    
    /** 权限代码，唯一标识符，格式如"module:action"，用于代码中的权限检查 */
    private String code;
    
    /** 权限名称，可读的权限描述，用于管理界面显示 */
    private String name;
    
    /** 资源标识，权限涉及的业务资源或模块名称 */
    private String resource;
    
    /** 操作类型，在资源上执行的具体操作，如read、write、delete等 */
    private String action;
    
    /** 权限描述，详细说明权限的作用和使用场景 */
    private String description;
    
    /** 是否启用，控制权限的有效性，支持权限的动态启用/禁用 */
    private Boolean enabled;
    
    /** 创建时间戳，记录权限的创建时间，用于审计和排序 */
    private Long createTime;
    
    /** 更新时间戳，记录权限的最后修改时间，用于变更追踪 */
    private Long updateTime;

    public Permission() {
        this.enabled = true;
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
    }

    public Permission(String code, String name, String resource, String action) {
        this();
        this.code = code;
        this.name = name;
        this.resource = resource;
        this.action = action;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }

    public Long getUpdateTime() { return updateTime; }
    public void setUpdateTime(Long updateTime) { this.updateTime = updateTime; }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", resource='" + resource + '\'' +
                ", action='" + action + '\'' +
                ", enabled=" + enabled +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return code != null && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}