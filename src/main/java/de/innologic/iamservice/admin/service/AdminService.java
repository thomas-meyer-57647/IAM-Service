package de.innologic.iamservice.admin.service;

import de.innologic.iamservice.admin.entity.IamSystemAdminEntity;
import de.innologic.iamservice.admin.entity.IamTenantAdminEntity;
import de.innologic.iamservice.admin.repo.IamSystemAdminRepository;
import de.innologic.iamservice.admin.repo.IamTenantAdminRepository;
import de.innologic.iamservice.assignment.repo.IamAssignmentRepository;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.permversion.service.PermVersionService;
import de.innologic.iamservice.security.CurrentPrincipal;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import de.innologic.iamservice.subject.service.SubjectService;
import de.innologic.iamservice.persistence.softdelete.SoftDeleteService;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final SubjectService subjectService;
    private final IamSystemAdminRepository systemAdminRepo;
    private final IamTenantAdminRepository tenantAdminRepo;
    private final SoftDeleteService softDeleteService;
    private final PermVersionService permVersionService;
    private final IamAssignmentRepository assignmentRepo;

    public AdminService(SubjectService subjectService,
                        IamSystemAdminRepository systemAdminRepo,
                        IamTenantAdminRepository tenantAdminRepo,
                        SoftDeleteService softDeleteService,
                        PermVersionService permVersionService,
                        IamAssignmentRepository assignmentRepo) {
        this.subjectService = subjectService;
        this.systemAdminRepo = systemAdminRepo;
        this.tenantAdminRepo = tenantAdminRepo;
        this.softDeleteService = softDeleteService;
        this.permVersionService = permVersionService;
        this.assignmentRepo = assignmentRepo;
    }

    @Transactional
    public void addSystemAdmin(String subjectId, SubjectType type) {
        IamSubjectEntity s = subjectService.getOrCreate(subjectId, type);
        Long subjectPk = s.getId();
        if (systemAdminRepo.findBySubject_Id(subjectPk).isPresent()) {
            return;
        }
        String actor = CurrentPrincipal.subjectId().orElse("system");
        if (systemAdminRepo.restoreDeleted(subjectPk, actor) == 0) {
            IamSystemAdminEntity a = new IamSystemAdminEntity();
            a.setSubject(s);
            systemAdminRepo.save(a);
        }

        assignmentRepo.findDistinctTenantIdsBySubjectId(subjectPk)
                .forEach(tenantId -> permVersionService.incrementForSubject(tenantId, subjectPk));
    }

    @Transactional
    public void addTenantAdmin(String tenantId, String subjectId, SubjectType type) {
        IamSubjectEntity s = subjectService.getOrCreate(subjectId, type);
        Long subjectPk = s.getId();
        if (tenantAdminRepo.findByTenantIdAndSubject_Id(tenantId, subjectPk).isPresent()) {
            return;
        }
        String actor = CurrentPrincipal.subjectId().orElse("system");
        if (tenantAdminRepo.restoreDeleted(tenantId, subjectPk, actor) == 0) {
            IamTenantAdminEntity a = new IamTenantAdminEntity();
            a.setTenantId(tenantId);
            a.setSubject(s);
            tenantAdminRepo.save(a);
        }
        permVersionService.incrementForSubject(tenantId, subjectPk);
    }

    @Transactional
    public void removeTenantAdmin(String tenantId, String subjectId, SubjectType type) {
        IamSubjectEntity s = subjectService.getOrCreate(subjectId, type);

        long count = tenantAdminRepo.countByTenantId(tenantId);
        if (count <= 1) {
            throw new IllegalStateException("tenant must have at least one admin");
        }

        IamTenantAdminEntity a = tenantAdminRepo.findByTenantIdAndSubject_Id(tenantId, s.getId())
                .orElseThrow(() -> new IllegalArgumentException("not tenant admin"));

        // Soft delete statt Hard delete
        softDeleteService.softDelete((JpaRepository<IamTenantAdminEntity, Long>) tenantAdminRepo, a);
        permVersionService.incrementForSubject(tenantId, s.getId());
    }

    public boolean isSystemAdmin(Long subjectPk) {
        return systemAdminRepo.findBySubject_Id(subjectPk).isPresent();
    }

    public boolean isTenantAdmin(String tenantId, Long subjectPk) {
        return tenantAdminRepo.findByTenantIdAndSubject_Id(tenantId, subjectPk).isPresent();
    }
}
