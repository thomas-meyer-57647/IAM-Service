package de.innologic.iamservice.role.entity;

import de.innologic.iamservice.permission.entity.IamPermissionEntity;
import de.innologic.iamservice.persistence.model.BaseAuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "iam_role_permission")
@SQLRestriction("deleted_at is null")
public class IamRolePermissionEntity extends BaseAuditableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private IamRoleEntity role;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private IamPermissionEntity permission;

    public IamRoleEntity getRole() { return role; }
    public void setRole(IamRoleEntity role) { this.role = role; }

    public IamPermissionEntity getPermission() { return permission; }
    public void setPermission(IamPermissionEntity permission) { this.permission = permission; }
}
