package de.innologic.iamservice.admin.repo;

import de.innologic.iamservice.admin.entity.IamSystemAdminEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IamSystemAdminRepository extends JpaRepository<IamSystemAdminEntity, Long> {
    Optional<IamSystemAdminEntity> findBySubject_Id(Long subjectPk);

    @Modifying
    @Query(value = """
            update iam_system_admin
            set deleted_at = null,
                deleted_by = null,
                modified_at = current_timestamp(3),
                modified_by = :actor
            where subject_pk = :subjectPk
              and deleted_at is not null
            limit 1
            """, nativeQuery = true)
    int restoreDeleted(@Param("subjectPk") Long subjectPk, @Param("actor") String actor);
}
