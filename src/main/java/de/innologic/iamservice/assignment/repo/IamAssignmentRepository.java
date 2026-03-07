package de.innologic.iamservice.assignment.repo;

import de.innologic.iamservice.assignment.entity.IamAssignmentEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface IamAssignmentRepository extends JpaRepository<IamAssignmentEntity, Long> {
    List<IamAssignmentEntity> findByTenantIdAndSubject_Id(String tenantId, Long subjectPk);

    boolean existsByTenantIdAndSubject_IdAndRole_IdAndScopeTypeIsNullAndScopeIdIsNull(String tenantId, Long subjectPk, Long roleId);

    @Query("select distinct a.subject.id from IamAssignmentEntity a where a.tenantId = :tenantId and a.role.id = :roleId")
    Set<Long> findDistinctSubjectIdsByTenantIdAndRoleId(@Param("tenantId") String tenantId, @Param("roleId") Long roleId);

    @Query("select distinct a.subject.id from IamAssignmentEntity a where a.tenantId = :tenantId")
    Set<Long> findDistinctSubjectIdsByTenantId(@Param("tenantId") String tenantId);

    @Query("select distinct a.tenantId from IamAssignmentEntity a where a.subject.id = :subjectPk")
    Set<String> findDistinctTenantIdsBySubjectId(@Param("subjectPk") Long subjectPk);

    @Modifying
    @Query(value = """
        update iam_assignment
        set deleted_at = null,
            deleted_by = null,
            scope_type_norm = '__NULL__',
            scope_id_norm = '__NULL__',
            active_key = 1,
            modified_at = current_timestamp(3),
            modified_by = :actor
        where tenant_id = :tenantId
          and subject_pk = :subjectPk
          and role_id = :roleId
          and scope_type is null
          and scope_id is null
          and deleted_at is not null
        limit 1
        """, nativeQuery = true)
    int restoreDeletedGlobalAssignment(@Param("tenantId") String tenantId,
                                       @Param("subjectPk") Long subjectPk,
                                       @Param("roleId") Long roleId,
                                       @Param("actor") String actor);
}
