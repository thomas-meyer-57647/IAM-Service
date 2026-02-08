package de.innologic.iamservice.assignment.repo;

import de.innologic.iamservice.assignment.entity.IamAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IamAssignmentRepository extends JpaRepository<IamAssignmentEntity, Long> {
    List<IamAssignmentEntity> findByTenantIdAndSubject_Id(String tenantId, Long subjectPk);
}
