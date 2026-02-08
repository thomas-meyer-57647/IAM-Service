package de.innologic.iamservice.admin.repo;

import de.innologic.iamservice.admin.entity.IamSystemAdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IamSystemAdminRepository extends JpaRepository<IamSystemAdminEntity, Long> {
    Optional<IamSystemAdminEntity> findBySubject_Id(Long subjectPk);
}
