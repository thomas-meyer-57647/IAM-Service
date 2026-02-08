package de.innologic.iamservice.subject.entity;

import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.persistence.model.BaseAuditableEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "iam_subject")
@SQLRestriction("deleted_at is null")
public class IamSubjectEntity extends BaseAuditableEntity {

    @Column(name = "subject_id", nullable = false, length = 128)
    private String subjectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 16)
    private SubjectType subjectType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public SubjectType getSubjectType() { return subjectType; }
    public void setSubjectType(SubjectType subjectType) { this.subjectType = subjectType; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
