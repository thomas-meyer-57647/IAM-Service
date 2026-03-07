package de.innologic.iamservice.tenant.entity;

import de.innologic.iamservice.module.entity.IamModuleEntity;
import de.innologic.iamservice.persistence.model.BaseAuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "iam_tenant_module")
@SQLRestriction("deleted_at is null")
public class IamTenantModuleEntity extends BaseAuditableEntity {

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private IamModuleEntity module;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "active_key")
    private Integer activeKey;

    @PrePersist
    @PreUpdate
    void syncDerivedColumns() {
        this.activeKey = getDeletedAt() == null ? 1 : null;
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public IamModuleEntity getModule() { return module; }
    public void setModule(IamModuleEntity module) { this.module = module; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Integer getActiveKey() { return activeKey; }
    public void setActiveKey(Integer activeKey) { this.activeKey = activeKey; }
}
