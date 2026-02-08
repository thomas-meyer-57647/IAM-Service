package de.innologic.iamservice.persistence.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedDate
    @Column(name = "modified_at")
    private Instant modifiedAt;

    @LastModifiedBy
    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    // --- helpers

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void markDeleted(String by) {
        if (this.deletedAt == null) {
            this.deletedAt = Instant.now();
            this.deletedBy = by;
        }
    }

    // --- getters/setters

    public Long getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public Instant getModifiedAt() { return modifiedAt; }
    public String getModifiedBy() { return modifiedBy; }
    public Instant getDeletedAt() { return deletedAt; }
    public String getDeletedBy() { return deletedBy; }

    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
}
