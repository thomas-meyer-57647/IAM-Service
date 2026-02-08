package de.innologic.iamservice.role.repo;

import de.innologic.iamservice.role.entity.IamRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IamRoleRepository extends JpaRepository<IamRoleEntity, Long> {
    List<IamRoleEntity> findByTenantId(String tenantId);
    Optional<IamRoleEntity> findByIdAndTenantId(Long id, String tenantId);
}
