package de.innologic.iamservice.tenant.repo;

import de.innologic.iamservice.tenant.entity.IamTenantModuleEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IamTenantModuleRepository extends JpaRepository<IamTenantModuleEntity, Long> {
    Optional<IamTenantModuleEntity> findByTenantIdAndModule_ModuleKey(String tenantId, String moduleKey);

    @Modifying
    @Query(value = """
        update iam_tenant_module
        set deleted_at = null,
            deleted_by = null,
            enabled = :enabled,
            active_key = 1,
            modified_at = current_timestamp(3),
            modified_by = :actor
        where tenant_id = :tenantId
          and module_id = :moduleId
          and deleted_at is not null
        limit 1
        """, nativeQuery = true)
    int restoreDeleted(@Param("tenantId") String tenantId, @Param("moduleId") Long moduleId, @Param("enabled") boolean enabled, @Param("actor") String actor);
}
