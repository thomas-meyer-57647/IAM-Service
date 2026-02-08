package de.innologic.iamservice.access.service;

import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.assignment.repo.IamAssignmentRepository;
import de.innologic.iamservice.catalog.service.CatalogService;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.permission.repo.IamPermissionRepository;
import de.innologic.iamservice.role.repo.IamRolePermissionRepository;
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
    private final IamAssignmentRepository assignmentRepo;
    private final IamRolePermissionRepository rolePermRepo;
    private final IamPermissionRepository permRepo;

    public AccessQueryService(CatalogService catalogService,
                              IamSubjectRepository subjectRepo,
                              AdminService adminService,
                              IamAssignmentRepository assignmentRepo,
                              IamRolePermissionRepository rolePermRepo,
                              IamPermissionRepository permRepo) {
        this.catalogService = catalogService;
        this.subjectRepo = subjectRepo;
        this.adminService = adminService;
        this.assignmentRepo = assignmentRepo;
        this.rolePermRepo = rolePermRepo;
        this.permRepo = permRepo;
    }

    @Transactional
    public List<String> getPermissions(String tenantId, String subjectId, SubjectType type, String moduleKey) {
        // 1) Module muss für Tenant enabled sein, sonst keine Rechte
        if (!catalogService.isTenantModuleEnabled(tenantId, moduleKey)) {
            return List.of();
        }

        // 2) Subject muss existieren
        IamSubjectEntity subject = subjectRepo.findBySubjectIdAndSubjectType(subjectId, type)
                .orElse(null);
        if (subject == null) return List.of();

        Long subjectPk = subject.getId();

        // 3) Tenant-Admin: alle Permissions im Modul
        if (adminService.isTenantAdmin(tenantId, subjectPk)) {
            return permRepo.findByModule_ModuleKey(moduleKey).stream()
                    .filter(p -> p.isActive())
                    .map(p -> p.getCode())
                    .sorted()
                    .toList();
        }

        // 4) Rollen-Assignments -> RolePermissions -> PermissionCodes (nur für dieses Modul)
        var assignments = assignmentRepo.findByTenantIdAndSubject_Id(tenantId, subjectPk);
        if (assignments.isEmpty()) return List.of();

        return assignments.stream()
                .flatMap(a -> rolePermRepo.findByRole_Id(a.getRole().getId()).stream())
                .map(rp -> rp.getPermission())
                .filter(p -> p.getModule().getModuleKey().equals(moduleKey))
                .filter(p -> p.isActive())
                .map(p -> p.getCode())
                .distinct()
                .sorted()
                .toList();
    }
}
