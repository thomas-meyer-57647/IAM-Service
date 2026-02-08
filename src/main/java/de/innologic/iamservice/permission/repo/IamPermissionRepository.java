package de.innologic.iamservice.permission.repo;

import de.innologic.iamservice.permission.entity.IamPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IamPermissionRepository extends JpaRepository<IamPermissionEntity, Long> {
    List<IamPermissionEntity> findByModule_ModuleKey(String moduleKey);
    List<IamPermissionEntity> findByModule_Id(Long moduleId);
}
