package de.innologic.iamservice.role.service;

import de.innologic.iamservice.assignment.entity.IamAssignmentEntity;
import de.innologic.iamservice.assignment.repo.IamAssignmentRepository;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.permission.entity.IamPermissionEntity;
import de.innologic.iamservice.permission.repo.IamPermissionRepository;
import de.innologic.iamservice.persistence.softdelete.SoftDeleteService;
import de.innologic.iamservice.role.entity.IamRoleEntity;
import de.innologic.iamservice.role.entity.IamRolePermissionEntity;
import de.innologic.iamservice.role.repo.IamRolePermissionRepository;
import de.innologic.iamservice.role.repo.IamRoleRepository;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import de.innologic.iamservice.subject.service.SubjectService;
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

    public RoleService(IamRoleRepository roleRepo,
                       IamPermissionRepository permRepo,
                       IamRolePermissionRepository rolePermRepo,
                       SoftDeleteService softDeleteService,
                       SubjectService subjectService,
                       IamAssignmentRepository assignmentRepo) {
        this.roleRepo = roleRepo;
        this.permRepo = permRepo;
        this.rolePermRepo = rolePermRepo;
        this.softDeleteService = softDeleteService;
        this.subjectService = subjectService;
        this.assignmentRepo = assignmentRepo;
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

        // 1) existierende RolePermissions soft-deleten (kein Hard-Delete!)
        List<IamRolePermissionEntity> existing = rolePermRepo.findByRole_Id(role.getId());
        for (IamRolePermissionEntity rp : existing) {
            softDeleteService.softDelete(rolePermRepo, rp);
        }

        // 2) neue Permissions sauber aus DB laden
        Set<String> requested = permissionCodes == null ? Set.of()
                : permissionCodes.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isBlank()).collect(Collectors.toSet());

        if (requested.isEmpty()) {
            return; // Rolle hat dann keine Permissions
        }

        List<IamPermissionEntity> perms = permRepo.findByCodeInAndActiveTrue(requested);

        Set<String> found = perms.stream().map(IamPermissionEntity::getCode).collect(Collectors.toSet());
        if (found.size() != requested.size()) {
            Set<String> missing = new TreeSet<>(requested);
            missing.removeAll(found);
            throw new EntityNotFoundException("permission(s) not found or inactive: " + missing);
        }

        // 3) neue Mappings anlegen
        for (IamPermissionEntity p : perms) {
            IamRolePermissionEntity rp = new IamRolePermissionEntity();
            rp.setRole(role);
            rp.setPermission(p);
            rolePermRepo.save(rp);
        }
    }

    @Transactional
    public void assignRole(String tenantId, String subjectId, SubjectType subjectType, Long roleId) {
        IamRoleEntity role = roleRepo.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("role not found"));

        IamSubjectEntity subject = subjectService.getOrCreate(subjectId, subjectType);

        IamAssignmentEntity a = new IamAssignmentEntity();
        a.setTenantId(tenantId);
        a.setSubject(subject);
        a.setRole(role);
        assignmentRepo.save(a);
    }
}
