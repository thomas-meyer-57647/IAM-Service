package de.innologic.iamservice.permission.entity;

import de.innologic.iamservice.module.entity.IamModuleEntity;
import de.innologic.iamservice.persistence.model.BaseAuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "iam_permission")
@SQLRestriction("deleted_at is null")
public class IamPermissionEntity extends BaseAuditableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private IamModuleEntity module;

    @Column(name = "code", nullable = false, length = 128)
    private String code;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public IamModuleEntity getModule() { return module; }
    public void setModule(IamModuleEntity module) { this.module = module; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
