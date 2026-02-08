package de.innologic.iamservice.module.repo;

import de.innologic.iamservice.module.entity.IamModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IamModuleRepository extends JpaRepository<IamModuleEntity, Long> {
    Optional<IamModuleEntity> findByModuleKey(String moduleKey);
}
