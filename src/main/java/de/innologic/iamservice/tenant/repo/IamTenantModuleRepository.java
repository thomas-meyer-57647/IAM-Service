package de.innologic.iamservice.tenant.repo;

import de.innologic.iamservice.tenant.entity.IamTenantModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IamTenantModuleRepository extends JpaRepository<IamTenantModuleEntity, Long> {
    Optional<IamTenantModuleEntity> findByTenantIdAndModule_ModuleKey(String tenantId, String moduleKey);
}
