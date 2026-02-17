package de.innologic.iamservice.role;

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
import de.innologic.iamservice.role.service.RoleService;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import de.innologic.iamservice.subject.service.SubjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceTest {

    @Mock IamRoleRepository roleRepo;
    @Mock IamPermissionRepository permRepo;
    @Mock IamRolePermissionRepository rolePermRepo;
    @Mock SoftDeleteService softDeleteService;
    @Mock SubjectService subjectService;
    @Mock IamAssignmentRepository assignmentRepo;
    @Mock PermVersionService permVersionService;

    @InjectMocks RoleService roleService;

    @Test
    void assignRole_incrementsPermVersion() {
        IamRoleEntity role = role(10L, "tenantA");
        IamSubjectEntity subject = subject(100L, "user1");

        when(roleRepo.findByIdAndTenantId(10L, "tenantA")).thenReturn(Optional.of(role));
        when(subjectService.getOrCreate("user1", SubjectType.USER)).thenReturn(subject);
        when(assignmentRepo.existsByTenantIdAndSubject_IdAndRole_IdAndScopeTypeIsNullAndScopeIdIsNull("tenantA", 100L, 10L))
                .thenReturn(false);
        when(assignmentRepo.restoreDeletedGlobalAssignment("tenantA", 100L, 10L, "system")).thenReturn(0);

        roleService.assignRole("tenantA", "user1", SubjectType.USER, 10L);

        verify(assignmentRepo).save(ArgumentMatchers.any());
        verify(permVersionService).incrementForSubject("tenantA", 100L);
    }

    @Test
    void assignRole_restoresSoftDeletedInsteadOfInsert() {
        IamRoleEntity role = role(10L, "tenantA");
        IamSubjectEntity subject = subject(100L, "user1");

        when(roleRepo.findByIdAndTenantId(10L, "tenantA")).thenReturn(Optional.of(role));
        when(subjectService.getOrCreate("user1", SubjectType.USER)).thenReturn(subject);
        when(assignmentRepo.existsByTenantIdAndSubject_IdAndRole_IdAndScopeTypeIsNullAndScopeIdIsNull("tenantA", 100L, 10L))
                .thenReturn(false);
        when(assignmentRepo.restoreDeletedGlobalAssignment("tenantA", 100L, 10L, "system")).thenReturn(1);

        roleService.assignRole("tenantA", "user1", SubjectType.USER, 10L);

        verify(assignmentRepo, never()).save(ArgumentMatchers.any());
        verify(permVersionService).incrementForSubject("tenantA", 100L);
    }

    @Test
    void setRolePermissions_incrementsPermVersionForAffectedSubjects() {
        IamRoleEntity role = role(10L, "tenantA");
        IamPermissionEntity permission = permission(77L, "timeentry.read");

        when(roleRepo.findByIdAndTenantId(10L, "tenantA")).thenReturn(Optional.of(role));
        when(rolePermRepo.findByRole_Id(10L)).thenReturn(List.of());
        when(permRepo.findByCodeInAndActiveTrue(Set.of("timeentry.read"))).thenReturn(List.of(permission));
        when(rolePermRepo.restoreDeleted(10L, 77L, "system")).thenReturn(0);
        when(assignmentRepo.findDistinctSubjectIdsByTenantIdAndRoleId("tenantA", 10L)).thenReturn(Set.of(100L, 101L));

        roleService.setRolePermissions("tenantA", 10L, List.of("timeentry.read"));

        verify(rolePermRepo).save(ArgumentMatchers.any(IamRolePermissionEntity.class));
        verify(permVersionService).incrementForSubjects("tenantA", Set.of(100L, 101L));
    }

    @Test
    void setRolePermissions_restoresSoftDeletedInsteadOfInsert() {
        IamRoleEntity role = role(10L, "tenantA");
        IamPermissionEntity permission = permission(77L, "timeentry.read");

        when(roleRepo.findByIdAndTenantId(10L, "tenantA")).thenReturn(Optional.of(role));
        when(rolePermRepo.findByRole_Id(10L)).thenReturn(List.of());
        when(permRepo.findByCodeInAndActiveTrue(Set.of("timeentry.read"))).thenReturn(List.of(permission));
        when(rolePermRepo.restoreDeleted(10L, 77L, "system")).thenReturn(1);
        when(assignmentRepo.findDistinctSubjectIdsByTenantIdAndRoleId("tenantA", 10L)).thenReturn(Set.of(100L));

        roleService.setRolePermissions("tenantA", 10L, List.of("timeentry.read"));

        verify(rolePermRepo, never()).save(ArgumentMatchers.any(IamRolePermissionEntity.class));
        verify(permVersionService).incrementForSubjects("tenantA", Set.of(100L));
    }

    private static IamRoleEntity role(Long id, String tenantId) {
        IamRoleEntity role = mock(IamRoleEntity.class);
        when(role.getId()).thenReturn(id);
        when(role.getTenantId()).thenReturn(tenantId);
        return role;
    }

    private static IamSubjectEntity subject(Long id, String subjectId) {
        IamSubjectEntity subject = mock(IamSubjectEntity.class);
        when(subject.getId()).thenReturn(id);
        when(subject.getSubjectId()).thenReturn(subjectId);
        return subject;
    }

    private static IamPermissionEntity permission(Long id, String code) {
        IamPermissionEntity permission = mock(IamPermissionEntity.class);
        when(permission.getId()).thenReturn(id);
        when(permission.getCode()).thenReturn(code);
        when(permission.isActive()).thenReturn(true);
        return permission;
    }
}
