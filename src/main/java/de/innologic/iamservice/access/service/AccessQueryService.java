package de.innologic.iamservice.access.service;

import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.catalog.service.CatalogService;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.permversion.service.PermVersionService;
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
    private final PermVersionService permVersionService;

    public AccessQueryService(CatalogService catalogService,
                              IamSubjectRepository subjectRepo,
                              AdminService adminService,
                              IamPermissionRepository permRepo,
                              PermVersionService permVersionService) {
        this.catalogService = catalogService;
        this.subjectRepo = subjectRepo;
        this.adminService = adminService;
        this.permRepo = permRepo;
        this.permVersionService = permVersionService;
    }

    @Transactional
    public AccessQueryResult getAccess(String tenantId, String subjectId, SubjectType type, String moduleKey) {

        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
        if (moduleKey == null || moduleKey.isBlank()) {
            throw new IllegalArgumentException("moduleKey must not be blank");
        }

        // Modul muss für Tenant enabled sein
        if (!catalogService.isTenantModuleEnabled(tenantId, moduleKey)) {
            return new AccessQueryResult(false, List.of(), 1L);
        }

        IamSubjectEntity subject = subjectRepo.findBySubjectIdAndSubjectType(subjectId, type).orElse(null);
        if (subject == null) return new AccessQueryResult(true, List.of(), 1L);

        Long subjectPk = subject.getId();
        long permVersion = permVersionService.getCurrentVersion(tenantId, subjectPk);

        // Tenant-Admin: alle Permissions im Modul
        if (adminService.isTenantAdmin(tenantId, subjectPk)) {
            List<String> permissions = permRepo.findByModule_ModuleKey(moduleKey).stream()
                    .filter(p -> p.isActive())
                    .map(p -> p.getCode())
                    .distinct()
                    .sorted()
                    .toList();
            return new AccessQueryResult(true, permissions, permVersion);
        }

        // Normalfall: 1 DB Query für effektive Permission-Codes
        return new AccessQueryResult(true, permRepo.findEffectivePermissionCodes(tenantId, subjectPk, moduleKey), permVersion);
    }

    public record AccessQueryResult(boolean enabled, List<String> permissions, long permVersion) {
    }
}
