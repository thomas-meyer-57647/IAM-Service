package de.innologic.iamservice.admin.repo;

import de.innologic.iamservice.admin.entity.IamTenantAdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IamTenantAdminRepository extends JpaRepository<IamTenantAdminEntity, Long> {
    long countByTenantId(String tenantId);
    Optional<IamTenantAdminEntity> findByTenantIdAndSubject_Id(String tenantId, Long subjectPk);
}
