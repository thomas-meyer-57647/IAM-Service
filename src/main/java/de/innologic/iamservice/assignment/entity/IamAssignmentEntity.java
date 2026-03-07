package de.innologic.iamservice.assignment.entity;

import de.innologic.iamservice.persistence.model.BaseAuditableEntity;
import de.innologic.iamservice.role.entity.IamRoleEntity;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "iam_assignment")
@SQLRestriction("deleted_at is null")
public class IamAssignmentEntity extends BaseAuditableEntity {

    public static final String NULL_MARKER = "__NULL__";

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_pk", nullable = false)
    private IamSubjectEntity subject;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private IamRoleEntity role;

    @Column(name = "scope_type", length = 32)
    private String scopeType;

    @Column(name = "scope_id", length = 128)
    private String scopeId;

    @Column(name = "scope_type_norm", nullable = false, length = 32)
    private String scopeTypeNorm;

    @Column(name = "scope_id_norm", nullable = false, length = 128)
    private String scopeIdNorm;

    @Column(name = "active_key")
    private Integer activeKey;

    @PrePersist
    @PreUpdate
    void syncDerivedColumns() {
        this.scopeTypeNorm = scopeType == null ? NULL_MARKER : scopeType;
        this.scopeIdNorm = scopeId == null ? NULL_MARKER : scopeId;
        this.activeKey = getDeletedAt() == null ? 1 : null;
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public IamSubjectEntity getSubject() { return subject; }
    public void setSubject(IamSubjectEntity subject) { this.subject = subject; }

    public IamRoleEntity getRole() { return role; }
    public void setRole(IamRoleEntity role) { this.role = role; }

    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }

    public String getScopeId() { return scopeId; }
    public void setScopeId(String scopeId) { this.scopeId = scopeId; }

    public String getScopeTypeNorm() { return scopeTypeNorm; }
    public void setScopeTypeNorm(String scopeTypeNorm) { this.scopeTypeNorm = scopeTypeNorm; }

    public String getScopeIdNorm() { return scopeIdNorm; }
    public void setScopeIdNorm(String scopeIdNorm) { this.scopeIdNorm = scopeIdNorm; }

    public Integer getActiveKey() { return activeKey; }
    public void setActiveKey(Integer activeKey) { this.activeKey = activeKey; }
}
