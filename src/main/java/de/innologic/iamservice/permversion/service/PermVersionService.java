package de.innologic.iamservice.permversion.service;

import de.innologic.iamservice.permversion.repo.IamPermVersionRepository;
import de.innologic.iamservice.security.CurrentPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class PermVersionService {

    private final IamPermVersionRepository permVersionRepo;

    public PermVersionService(IamPermVersionRepository permVersionRepo) {
        this.permVersionRepo = permVersionRepo;
    }

    @Transactional
    public long getCurrentVersion(String tenantId, Long subjectPk) {
        return permVersionRepo.findByTenantIdAndSubject_Id(tenantId, subjectPk)
                .map(v -> v.getVersion())
                .orElse(1L);
    }

    @Transactional
    public void incrementForSubject(String tenantId, Long subjectPk) {
        String actor = CurrentPrincipal.subjectId().orElse("system");
        permVersionRepo.upsertAndIncrement(tenantId, subjectPk, actor);
    }

    @Transactional
    public void incrementForSubjects(String tenantId, Collection<Long> subjectPks) {
        if (subjectPks == null || subjectPks.isEmpty()) {
            return;
        }
        for (Long subjectPk : subjectPks) {
            if (subjectPk != null) {
                incrementForSubject(tenantId, subjectPk);
            }
        }
    }
}
