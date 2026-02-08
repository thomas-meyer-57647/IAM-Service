package de.innologic.iamservice.role.service;

import de.innologic.iamservice.assignment.entity.IamAssignmentEntity;
import de.innologic.iamservice.assignment.repo.IamAssignmentRepository;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.permission.entity.IamPermissionEntity;
import de.innologic.iamservice.permission.repo.IamPermissionRepository;
import de.innologic.iamservice.role.entity.IamRoleEntity;
import de.innologic.iamservice.role.entity.IamRolePermissionEntity;
import de.innologic.iamservice.role.repo.IamRolePermissionRepository;
import de.innologic.iamservice.role.repo.IamRoleRepository;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import de.innologic.iamservice.subject.service.SubjectService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final IamRoleRepository roleRepo;
    private final IamPermissionRepository permRepo;
    private final IamRolePermissionRepository rolePermRepo;
    private final SubjectService subjectService;
    private final IamAssignmentRepository assignmentRepo;

    public RoleService(IamRoleRepository roleRepo,
                       IamPermissionRepository permRepo,
                       IamRolePermissionRepository rolePermRepo,
                       SubjectService subjectService,
                       IamAssignmentRepository assignmentRepo) {
        this.roleRepo = roleRepo;
        this.permRepo = permRepo;
        this.rolePermRepo = rolePermRepo;
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

        rolePermRepo.deleteByRole_Id(role.getId());

        // Permission Codes -> Entities
        for (String code : permissionCodes) {
            IamPermissionEntity p = permRepo.findAll().stream()
                    .filter(x -> x.getCode().equals(code))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("permission not found: " + code));

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
