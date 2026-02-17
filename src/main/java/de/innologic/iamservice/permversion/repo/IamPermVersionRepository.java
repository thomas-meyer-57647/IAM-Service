package de.innologic.iamservice.permversion.repo;

import de.innologic.iamservice.permversion.entity.IamPermVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IamPermVersionRepository extends JpaRepository<IamPermVersionEntity, Long> {

    Optional<IamPermVersionEntity> findByTenantIdAndSubject_Id(String tenantId, Long subjectPk);

    @Modifying
    @Query(value = """
            insert into iam_perm_version(
              tenant_id, subject_pk, version, created_at, modified_at, created_by, modified_by
            ) values (
              :tenantId, :subjectPk, 2, current_timestamp(3), current_timestamp(3), :actor, :actor
            )
            on duplicate key update
              version = version + 1,
              modified_at = current_timestamp(3),
              modified_by = :actor
            """, nativeQuery = true)
    void upsertAndIncrement(@Param("tenantId") String tenantId, @Param("subjectPk") Long subjectPk, @Param("actor") String actor);
}
