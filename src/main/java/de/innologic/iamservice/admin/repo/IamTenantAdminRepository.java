package de.innologic.iamservice.admin.repo;

import de.innologic.iamservice.admin.entity.IamTenantAdminEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface IamTenantAdminRepository extends JpaRepository<IamTenantAdminEntity, Long> {
    long countByTenantId(String tenantId);
    Optional<IamTenantAdminEntity> findByTenantIdAndSubject_Id(String tenantId, Long subjectPk);
    @Query("select distinct a.subject.id from IamTenantAdminEntity a where a.tenantId = :tenantId")
    Set<Long> findDistinctSubjectIdsByTenantId(@Param("tenantId") String tenantId);

    @Modifying
    @Query(value = """
            update iam_tenant_admin
            set deleted_at = null,
                deleted_by = null,
                modified_at = current_timestamp(3),
                modified_by = :actor
            where tenant_id = :tenantId
              and subject_pk = :subjectPk
              and deleted_at is not null
            limit 1
            """, nativeQuery = true)
    int restoreDeleted(@Param("tenantId") String tenantId, @Param("subjectPk") Long subjectPk, @Param("actor") String actor);
}
