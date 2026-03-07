package de.innologic.iamservice.admin;

import de.innologic.iamservice.admin.entity.IamSystemAdminEntity;
import de.innologic.iamservice.admin.entity.IamTenantAdminEntity;
import de.innologic.iamservice.admin.repo.IamSystemAdminRepository;
import de.innologic.iamservice.admin.repo.IamTenantAdminRepository;
import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.assignment.repo.IamAssignmentRepository;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.permversion.service.PermVersionService;
import de.innologic.iamservice.persistence.softdelete.SoftDeleteService;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import de.innologic.iamservice.subject.service.SubjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminServiceTest {

    private static final String TENANT_ID = "COMPANY-100";
    private static final String SUBJECT_ID = "USR-123";
    private static final Long SUBJECT_PK = 42L;

    @Mock
    SubjectService subjectService;

    @Mock
    IamSystemAdminRepository systemAdminRepo;

    @Mock
    IamTenantAdminRepository tenantAdminRepo;

    @Mock
    SoftDeleteService softDeleteService;

    @Mock
    PermVersionService permVersionService;

    @Mock
    IamAssignmentRepository assignmentRepo;

    @InjectMocks
    AdminService adminService;

    @Mock
    IamSubjectEntity subject;

    @Captor
    ArgumentCaptor<IamSystemAdminEntity> systemAdminCaptor;

    @Captor
    ArgumentCaptor<IamTenantAdminEntity> tenantAdminCaptor;

    @BeforeEach
    void setUp() {
        when(subject.getId()).thenReturn(SUBJECT_PK);
        lenient().when(subjectService.getOrCreate(SUBJECT_ID, SubjectType.USER)).thenReturn(subject);
    }

    @Test
    void addSystemAdmin_createsNewEntryAndIncrementsPermVersion() {
        when(systemAdminRepo.findBySubject_Id(SUBJECT_PK)).thenReturn(Optional.empty());
        when(systemAdminRepo.restoreDeleted(anyLong(), anyString())).thenReturn(0);
        when(assignmentRepo.findDistinctTenantIdsBySubjectId(SUBJECT_PK)).thenReturn(Set.of("TENANT-A", "TENANT-B"));

        adminService.addSystemAdmin(SUBJECT_ID, SubjectType.USER);

        verify(systemAdminRepo).save(systemAdminCaptor.capture());
        assertSame(subject, systemAdminCaptor.getValue().getSubject());
        verify(permVersionService).incrementForSubject("TENANT-A", SUBJECT_PK);
        verify(permVersionService).incrementForSubject("TENANT-B", SUBJECT_PK);
    }

    @Test
    void addSystemAdmin_restoresSoftDeletedAndStillIncrements() {
        when(systemAdminRepo.findBySubject_Id(SUBJECT_PK)).thenReturn(Optional.empty());
        when(systemAdminRepo.restoreDeleted(SUBJECT_PK, "system")).thenReturn(1);
        when(assignmentRepo.findDistinctTenantIdsBySubjectId(SUBJECT_PK)).thenReturn(Set.of("TENANT-A"));

        adminService.addSystemAdmin(SUBJECT_ID, SubjectType.USER);

        verify(systemAdminRepo, never()).save(any());
        verify(permVersionService).incrementForSubject("TENANT-A", SUBJECT_PK);
    }

    @Test
    void addSystemAdmin_isIdempotentWhenAlreadyPresent() {
        when(systemAdminRepo.findBySubject_Id(SUBJECT_PK)).thenReturn(Optional.of(mock(IamSystemAdminEntity.class)));

        adminService.addSystemAdmin(SUBJECT_ID, SubjectType.USER);

        verify(systemAdminRepo, never()).restoreDeleted(anyLong(), anyString());
        verify(permVersionService, never()).incrementForSubject(anyString(), anyLong());
    }

    @Test
    void addTenantAdmin_createsNewEntry() {
        when(tenantAdminRepo.findByTenantIdAndSubject_Id(TENANT_ID, SUBJECT_PK)).thenReturn(Optional.empty());

        adminService.addTenantAdmin(TENANT_ID, SUBJECT_ID, SubjectType.USER);

        verify(tenantAdminRepo).save(tenantAdminCaptor.capture());
        assertSame(subject, tenantAdminCaptor.getValue().getSubject());
        verify(permVersionService).incrementForSubject(TENANT_ID, SUBJECT_PK);
    }

    @Test
    void addTenantAdmin_restoresSoftDeleted() {
        when(tenantAdminRepo.findByTenantIdAndSubject_Id(TENANT_ID, SUBJECT_PK)).thenReturn(Optional.empty());
        when(tenantAdminRepo.restoreDeleted(TENANT_ID, SUBJECT_PK, "system")).thenReturn(1);

        adminService.addTenantAdmin(TENANT_ID, SUBJECT_ID, SubjectType.USER);

        verify(tenantAdminRepo, never()).save(any());
        verify(permVersionService).incrementForSubject(TENANT_ID, SUBJECT_PK);
    }

    @Test
    void addTenantAdmin_idempotentWhenAlreadyActive() {
        when(tenantAdminRepo.findByTenantIdAndSubject_Id(TENANT_ID, SUBJECT_PK)).thenReturn(Optional.of(mock(IamTenantAdminEntity.class)));

        adminService.addTenantAdmin(TENANT_ID, SUBJECT_ID, SubjectType.USER);

        verify(tenantAdminRepo, never()).restoreDeleted(anyString(), anyLong(), anyString());
        verify(permVersionService, never()).incrementForSubject(anyString(), anyLong());
    }

    @Test
    void removeTenantAdmin_softDeletesAndIncrementsPermVersion() {
        when(tenantAdminRepo.countByTenantId(TENANT_ID)).thenReturn(2L);
        IamTenantAdminEntity entity = mock(IamTenantAdminEntity.class);
        when(entity.getSubject()).thenReturn(subject);
        when(tenantAdminRepo.findByTenantIdAndSubject_Id(TENANT_ID, SUBJECT_PK)).thenReturn(Optional.of(entity));

        adminService.removeTenantAdmin(TENANT_ID, SUBJECT_ID, SubjectType.USER);

        verify(softDeleteService).softDelete(any(), eq(entity));
        verify(permVersionService).incrementForSubject(TENANT_ID, SUBJECT_PK);
    }

    @Test
    void removeTenantAdmin_throwsWhenLastAdmin() {
        when(tenantAdminRepo.countByTenantId(TENANT_ID)).thenReturn(1L);

        assertThrows(IllegalStateException.class,
                () -> adminService.removeTenantAdmin(TENANT_ID, SUBJECT_ID, SubjectType.USER));

        verify(softDeleteService, never()).softDelete(any(), any());
        verify(permVersionService, never()).incrementForSubject(anyString(), anyLong());
    }

    @Test
    void removeTenantAdmin_throwsWhenSubjectNotTenantAdmin() {
        when(tenantAdminRepo.countByTenantId(TENANT_ID)).thenReturn(2L);
        when(tenantAdminRepo.findByTenantIdAndSubject_Id(TENANT_ID, SUBJECT_PK)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> adminService.removeTenantAdmin(TENANT_ID, SUBJECT_ID, SubjectType.USER));

        verify(softDeleteService, never()).softDelete(any(), any());
        verify(permVersionService, never()).incrementForSubject(anyString(), anyLong());
    }
}
