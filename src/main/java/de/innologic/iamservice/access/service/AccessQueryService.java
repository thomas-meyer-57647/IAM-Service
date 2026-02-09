package de.innologic.iamservice.access.service;

import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.catalog.service.CatalogService;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.permission.repo.IamPermissionRepository;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import de.innologic.iamservice.subject.repo.IamSubjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccessQueryService {

    private final CatalogService catalogService;
    private final IamSubjectRepository subjectRepo;
    private final AdminService adminService;
    private final IamPermissionRepository permRepo;

    public AccessQueryService(CatalogService catalogService,
                              IamSubjectRepository subjectRepo,
                              AdminService adminService,
                              IamPermissionRepository permRepo) {
        this.catalogService = catalogService;
        this.subjectRepo = subjectRepo;
        this.adminService = adminService;
        this.permRepo = permRepo;
    }

    @Transactional
    public List<String> getPermissions(String tenantId, String subjectId, SubjectType type, String moduleKey) {

        // Modul muss für Tenant enabled sein
        if (!catalogService.isTenantModuleEnabled(tenantId, moduleKey)) {
            return List.of();
        }

        IamSubjectEntity subject = subjectRepo.findBySubjectIdAndSubjectType(subjectId, type).orElse(null);
        if (subject == null) return List.of();

        Long subjectPk = subject.getId();

        // Tenant-Admin: alle Permissions im Modul
        if (adminService.isTenantAdmin(tenantId, subjectPk)) {
            return permRepo.findByModule_ModuleKey(moduleKey).stream()
                    .filter(p -> p.isActive())
                    .map(p -> p.getCode())
                    .distinct()
                    .sorted()
                    .toList();
        }

        // Normalfall: 1 DB Query für effektive Permission-Codes
        return permRepo.findEffectivePermissionCodes(tenantId, subjectPk, moduleKey);
    }
}
