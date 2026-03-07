package de.innologic.iamservice.role.repo;

import de.innologic.iamservice.role.entity.IamRolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface IamRolePermissionRepository extends JpaRepository<IamRolePermissionEntity, Long> {

    List<IamRolePermissionEntity> findByRole_Id(Long roleId);

    boolean existsByRole_IdAndPermission_Id(Long roleId, Long permissionId);

    Set<IamRolePermissionEntity> findByRole_IdAndPermission_IdIn(Long roleId, Set<Long> permissionIds);

    @Modifying
    @Query(value = """
            update iam_role_permission
            set deleted_at = null,
                deleted_by = null,
                active_key = 1,
                modified_at = current_timestamp(3),
                modified_by = :actor
            where role_id = :roleId
              and permission_id = :permissionId
              and deleted_at is not null
            limit 1
            """, nativeQuery = true)
    int restoreDeleted(@Param("roleId") Long roleId,
                       @Param("permissionId") Long permissionId,
                       @Param("actor") String actor);
}
