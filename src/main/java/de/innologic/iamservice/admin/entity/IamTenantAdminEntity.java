package de.innologic.iamservice.admin.entity;

import de.innologic.iamservice.persistence.model.BaseAuditableEntity;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "iam_tenant_admin")
@SQLRestriction("deleted_at is null")
public class IamTenantAdminEntity extends BaseAuditableEntity {

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_pk", nullable = false)
    private IamSubjectEntity subject;

    @Column(name = "active_key")
    private Integer activeKey;

    @PrePersist
    @PreUpdate
    void syncDerivedColumns() {
        this.activeKey = getDeletedAt() == null ? 1 : null;
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public IamSubjectEntity getSubject() { return subject; }
    public void setSubject(IamSubjectEntity subject) { this.subject = subject; }

    public Integer getActiveKey() { return activeKey; }
    public void setActiveKey(Integer activeKey) { this.activeKey = activeKey; }
}
