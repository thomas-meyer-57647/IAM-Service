package de.innologic.iamservice.access;

import de.innologic.iamservice.access.service.AccessQueryService;
import de.innologic.iamservice.catalog.service.CatalogService;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.permission.entity.IamPermissionEntity;
import de.innologic.iamservice.permission.repo.IamPermissionRepository;
import de.innologic.iamservice.permversion.service.PermVersionService;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import de.innologic.iamservice.subject.repo.IamSubjectRepository;
import de.innologic.iamservice.admin.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessQueryServiceTest {

    @Mock
    CatalogService catalogService;
    @Mock
    IamSubjectRepository subjectRepo;
    @Mock
    AdminService adminService;
    @Mock
    IamPermissionRepository permRepo;
    @Mock
    PermVersionService permVersionService;

    @InjectMocks
    AccessQueryService accessQueryService;

    private static final String TENANT_ID = "COMPANY-100";
    private static final String SUBJECT_ID = "USR-1";
    private static final String MODULE_KEY = "user";
    private static final SubjectType TYPE = SubjectType.USER;

    @Mock
    IamSubjectEntity subject;

    @Test
    void moduleDisabled_returnsDisabled() {
        when(catalogService.isTenantModuleEnabled(TENANT_ID, MODULE_KEY)).thenReturn(false);

        var result = accessQueryService.getAccess(TENANT_ID, SUBJECT_ID, TYPE, MODULE_KEY);

        assertThat(result.enabled()).isFalse();
        assertThat(result.permissions()).isEmpty();
        assertThat(result.permVersion()).isEqualTo(1L);
        verifyNoInteractions(permVersionService);
    }

    @Test
    void subjectUnknown_returnsEnabledEmptyPermissions() {
        when(catalogService.isTenantModuleEnabled(TENANT_ID, MODULE_KEY)).thenReturn(true);
        when(subjectRepo.findBySubjectIdAndSubjectType(SUBJECT_ID, TYPE)).thenReturn(Optional.empty());

        var result = accessQueryService.getAccess(TENANT_ID, SUBJECT_ID, TYPE, MODULE_KEY);

        assertThat(result.enabled()).isTrue();
        assertThat(result.permissions()).isEmpty();
        assertThat(result.permVersion()).isEqualTo(1L);
        verifyNoInteractions(permVersionService);
    }

    @Test
    void tenantAdminGetsAllActivePermissions() {
        when(catalogService.isTenantModuleEnabled(TENANT_ID, MODULE_KEY)).thenReturn(true);
        when(subjectRepo.findBySubjectIdAndSubjectType(SUBJECT_ID, TYPE)).thenReturn(Optional.of(subject));
        when(permVersionService.getCurrentVersion(TENANT_ID, 55L)).thenReturn(10L);
        when(adminService.isTenantAdmin(TENANT_ID, 55L)).thenReturn(true);
        stubSubjectId();
        IamPermissionEntity p1 = mock(IamPermissionEntity.class);
        IamPermissionEntity p2 = mock(IamPermissionEntity.class);
        when(p1.isActive()).thenReturn(true);
        when(p1.getCode()).thenReturn("user.a");
        when(p2.isActive()).thenReturn(true);
        when(p2.getCode()).thenReturn("user.b");
        when(permRepo.findByModule_ModuleKey(MODULE_KEY)).thenReturn(List.of(p2, p1));

        var result = accessQueryService.getAccess(TENANT_ID, SUBJECT_ID, TYPE, MODULE_KEY);

        assertThat(result.enabled()).isTrue();
        assertThat(result.permissions()).containsExactly("user.a", "user.b");
        assertThat(result.permVersion()).isEqualTo(10L);
    }

    @Test
    void normalReturnsEffectivePermissionsAndVersion() {
        stubSubjectId();
        when(catalogService.isTenantModuleEnabled(TENANT_ID, MODULE_KEY)).thenReturn(true);
        when(subjectRepo.findBySubjectIdAndSubjectType(SUBJECT_ID, TYPE)).thenReturn(Optional.of(subject));
        when(permVersionService.getCurrentVersion(TENANT_ID, 55L)).thenReturn(99L);
        when(adminService.isTenantAdmin(TENANT_ID, 55L)).thenReturn(false);
        when(permRepo.findEffectivePermissionCodes(TENANT_ID, 55L, MODULE_KEY)).thenReturn(List.of("user.read"));

        var result = accessQueryService.getAccess(TENANT_ID, SUBJECT_ID, TYPE, MODULE_KEY);

        assertThat(result.enabled()).isTrue();
        assertThat(result.permissions()).containsExactly("user.read");
        assertThat(result.permVersion()).isEqualTo(99L);
    }

    @Test
    void invalidTenantId_throws() {
        assertThatThrownBy(() -> accessQueryService.getAccess("", SUBJECT_ID, TYPE, MODULE_KEY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenantId must not be blank");
    }

    private void stubSubjectId() {
        when(subject.getId()).thenReturn(55L);
    }
}
