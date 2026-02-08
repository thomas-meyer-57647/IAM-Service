package de.innologic.iamservice.role.repo;

import de.innologic.iamservice.role.entity.IamRolePermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IamRolePermissionRepository extends JpaRepository<IamRolePermissionEntity, Long> {
    List<IamRolePermissionEntity> findByRole_Id(Long roleId);
    void deleteByRole_Id(Long roleId);
}
