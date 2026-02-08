package de.innologic.iamservice.subject.service;

import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import de.innologic.iamservice.subject.repo.IamSubjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class SubjectService {

    private final IamSubjectRepository subjectRepo;

    public SubjectService(IamSubjectRepository subjectRepo) {
        this.subjectRepo = subjectRepo;
    }

    @Transactional
    public IamSubjectEntity getOrCreate(String subjectId, SubjectType type) {
        return subjectRepo.findBySubjectIdAndSubjectType(subjectId, type)
                .orElseGet(() -> {
                    IamSubjectEntity s = new IamSubjectEntity();
                    s.setSubjectId(subjectId);
                    s.setSubjectType(type);
                    s.setActive(true);
                    return subjectRepo.save(s);
                });
    }
}
