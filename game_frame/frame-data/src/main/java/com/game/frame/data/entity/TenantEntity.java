package com.game.frame.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * 多租户实体基类
 * @author lx
 * @date 2025/06/08
 */
@MappedSuperclass
public abstract class TenantEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId; // 租户ID

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{tenantId=" + tenantId + 
               ", " + super.toString() + "}";
    }
}