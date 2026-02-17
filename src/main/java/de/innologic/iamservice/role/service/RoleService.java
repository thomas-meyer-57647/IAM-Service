package de.innologic.iamservice.role.service;

import de.innologic.iamservice.assignment.entity.IamAssignmentEntity;
import de.innologic.iamservice.assignment.repo.IamAssignmentRepository;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.permversion.service.PermVersionService;
import de.innologic.iamservice.permission.entity.IamPermissionEntity;
import de.innologic.iamservice.permission.repo.IamPermissionRepository;
import de.innologic.iamservice.persistence.softdelete.SoftDeleteService;
import de.innologic.iamservice.role.entity.IamRoleEntity;
import de.innologic.iamservice.role.entity.IamRolePermissionEntity;
import de.innologic.iamservice.role.repo.IamRolePermissionRepository;
import de.innologic.iamservice.role.repo.IamRoleRepository;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import de.innologic.iamservice.subject.service.SubjectService;
import de.innologic.iamservice.security.CurrentPrincipal;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final IamRoleRepository roleRepo;
    private final IamPermissionRepository permRepo;
    private final IamRolePermissionRepository rolePermRepo;
    private final SoftDeleteService softDeleteService;

    private final SubjectService subjectService;
    private final IamAssignmentRepository assignmentRepo;
    private final PermVersionService permVersionService;

    public RoleService(IamRoleRepository roleRepo,
                       IamPermissionRepository permRepo,
                       IamRolePermissionRepository rolePermRepo,
                       SoftDeleteService softDeleteService,
                       SubjectService subjectService,
                       IamAssignmentRepository assignmentRepo,
                       PermVersionService permVersionService) {
        this.roleRepo = roleRepo;
        this.permRepo = permRepo;
        this.rolePermRepo = rolePermRepo;
        this.softDeleteService = softDeleteService;
        this.subjectService = subjectService;
        this.assignmentRepo = assignmentRepo;
        this.permVersionService = permVersionService;
    }

    @Transactional
    public IamRoleEntity createRole(String tenantId, String name, String description) {
        IamRoleEntity r = new IamRoleEntity();
        r.setTenantId(tenantId);
        r.setName(name);
        r.setDescription(description);
        r.setActive(true);
        return roleRepo.save(r);
    }

    public List<IamRoleEntity> listRoles(String tenantId) {
        return roleRepo.findByTenantId(tenantId);
    }

    @Transactional
    public void setRolePermissions(String tenantId, Long roleId, List<String> permissionCodes) {
        IamRoleEntity role = roleRepo.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("role not found"));

        // 1) gewünschte Permissions laden
        Set<String> requested = permissionCodes == null ? Set.of()
                : permissionCodes.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isBlank()).collect(Collectors.toSet());
        List<IamPermissionEntity> perms = permRepo.findByCodeInAndActiveTrue(requested);

        Set<String> found = perms.stream().map(IamPermissionEntity::getCode).collect(Collectors.toSet());
        if (found.size() != requested.size()) {
            Set<String> missing = new TreeSet<>(requested);
            missing.removeAll(found);
            throw new EntityNotFoundException("permission(s) not found or inactive: " + missing);
        }

        // 2) existierende RolePermissions diffen und nur Delta ändern
        Set<Long> requestedPermissionIds = perms.stream().map(IamPermissionEntity::getId).collect(Collectors.toSet());
        List<IamRolePermissionEntity> existing = rolePermRepo.findByRole_Id(role.getId());
        Set<Long> existingPermissionIds = existing.stream().map(rp -> rp.getPermission().getId()).collect(Collectors.toSet());

        for (IamRolePermissionEntity rp : existing) {
            if (!requestedPermissionIds.contains(rp.getPermission().getId())) {
                softDeleteService.softDelete(rolePermRepo, rp);
            }
        }

        String actor = CurrentPrincipal.subjectId().orElse("system");
        for (IamPermissionEntity p : perms) {
            if (existingPermissionIds.contains(p.getId())) {
                continue;
            }
            if (rolePermRepo.restoreDeleted(role.getId(), p.getId(), actor) > 0) {
                continue;
            }
            IamRolePermissionEntity rp = new IamRolePermissionEntity();
            rp.setRole(role);
            rp.setPermission(p);
            rolePermRepo.save(rp);
        }

        permVersionService.incrementForSubjects(tenantId, assignmentRepo.findDistinctSubjectIdsByTenantIdAndRoleId(tenantId, roleId));
    }

    @Transactional
    public void assignRole(String tenantId, String subjectId, SubjectType subjectType, Long roleId) {
        IamRoleEntity role = roleRepo.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("role not found"));

        IamSubjectEntity subject = subjectService.getOrCreate(subjectId, subjectType);
        Long subjectPk = subject.getId();

        if (!assignmentRepo.existsByTenantIdAndSubject_IdAndRole_IdAndScopeTypeIsNullAndScopeIdIsNull(tenantId, subjectPk, roleId)) {
            String actor = CurrentPrincipal.subjectId().orElse("system");
            if (assignmentRepo.restoreDeletedGlobalAssignment(tenantId, subjectPk, roleId, actor) == 0) {
                IamAssignmentEntity a = new IamAssignmentEntity();
                a.setTenantId(tenantId);
                a.setSubject(subject);
                a.setRole(role);
                assignmentRepo.save(a);
            }
        }
        permVersionService.incrementForSubject(tenantId, subjectPk);
    }
}
