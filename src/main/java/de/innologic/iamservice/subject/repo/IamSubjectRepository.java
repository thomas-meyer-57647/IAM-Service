package de.innologic.iamservice.subject.repo;

import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IamSubjectRepository extends JpaRepository<IamSubjectEntity, Long> {
    Optional<IamSubjectEntity> findBySubjectIdAndSubjectType(String subjectId, SubjectType subjectType);
}
