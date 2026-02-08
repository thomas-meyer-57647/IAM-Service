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
}
