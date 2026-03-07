package de.innologic.iamservice.permission.repo;

import de.innologic.iamservice.permission.entity.IamPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface IamPermissionRepository extends JpaRepository<IamPermissionEntity, Long> {

    List<IamPermissionEntity> findByModule_ModuleKey(String moduleKey);

    boolean existsByModule_ModuleKeyAndCode(String moduleKey, String code);

    // Für IAM-R05: sauberes Lookup (statt findAll()+filter)
    List<IamPermissionEntity> findByCodeInAndActiveTrue(Collection<String> codes);

    /**
     * Effektive Permission-Codes für Subject im Modul (über Assignments -> RolePermissions).
     * Hinweis: Soft-Delete wird durch @SQLRestriction in den Entities automatisch berücksichtigt.
     */
    @Query("""
      select distinct p.code
      from IamAssignmentEntity a
      join IamRolePermissionEntity rp on rp.role = a.role
      join rp.permission p
      where a.tenantId = :tenantId
        and a.subject.id = :subjectPk
        and p.module.moduleKey = :moduleKey
        and p.active = true
      order by p.code
    """)
    List<String> findEffectivePermissionCodes(
            @Param("tenantId") String tenantId,
            @Param("subjectPk") Long subjectPk,
            @Param("moduleKey") String moduleKey
    );
}
