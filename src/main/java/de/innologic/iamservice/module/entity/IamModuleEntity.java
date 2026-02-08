package de.innologic.iamservice.module.entity;

import de.innologic.iamservice.persistence.model.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "iam_module")
@SQLRestriction("deleted_at is null")
public class IamModuleEntity extends BaseAuditableEntity {

    @Column(name = "module_key", nullable = false, length = 64)
    private String moduleKey;

    @Column(name = "name", length = 128)
    private String name;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(String moduleKey) { this.moduleKey = moduleKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
