package de.innologic.iamservice.catalog;

import de.innologic.iamservice.admin.repo.IamTenantAdminRepository;
import de.innologic.iamservice.assignment.repo.IamAssignmentRepository;
import de.innologic.iamservice.catalog.service.CatalogService;
import de.innologic.iamservice.module.entity.IamModuleEntity;
import de.innologic.iamservice.module.repo.IamModuleRepository;
import de.innologic.iamservice.permission.entity.IamPermissionEntity;
import de.innologic.iamservice.permission.repo.IamPermissionRepository;
import de.innologic.iamservice.permversion.service.PermVersionService;
import de.innologic.iamservice.tenant.entity.IamTenantModuleEntity;
import de.innologic.iamservice.tenant.repo.IamTenantModuleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    private static final String MODULE_KEY = "user";
    private static final String TENANT_ID = "COMPANY-100";

    @Mock
    IamModuleRepository moduleRepo;

    @Mock
    IamPermissionRepository permRepo;

    @Mock
    IamTenantModuleRepository tenantModuleRepo;

    @Mock
    IamAssignmentRepository assignmentRepo;

    @Mock
    IamTenantAdminRepository tenantAdminRepo;

    @Mock
    PermVersionService permVersionService;

    @InjectMocks
    CatalogService catalogService;

    IamModuleEntity module;

    @BeforeEach
    void setUp() {
        module = new IamModuleEntity();
        module.setModuleKey(MODULE_KEY);
        module.setActive(true);
    }

    @Test
    void createModule_success() {
        when(moduleRepo.findByModuleKey(MODULE_KEY)).thenReturn(Optional.empty());
        when(moduleRepo.save(any(IamModuleEntity.class))).thenReturn(module);

        IamModuleEntity created = catalogService.createModule(MODULE_KEY, "User", "desc");

        verify(moduleRepo).save(any());
        assertThat(created).isSameAs(module);
    }

    @Test
    void createModule_duplicate() {
        when(moduleRepo.findByModuleKey(MODULE_KEY)).thenReturn(Optional.of(module));

        assertThatThrownBy(() -> catalogService.createModule(MODULE_KEY, "User", "desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("moduleKey already exists");

        verify(moduleRepo, never()).save(any());
    }

    @Test
    void listModules_returnsAll() {
        List<IamModuleEntity> modules = List.of(module);
        when(moduleRepo.findAll()).thenReturn(modules);

        assertThat(catalogService.listModules()).isEqualTo(modules);
    }

    @Test
    void createPermission_success() {
        IamPermissionEntity permission = new IamPermissionEntity();
        when(moduleRepo.findByModuleKey(MODULE_KEY)).thenReturn(Optional.of(module));
        when(permRepo.existsByModule_ModuleKeyAndCode(MODULE_KEY, "user.read")).thenReturn(false);
        when(permRepo.save(any(IamPermissionEntity.class))).thenReturn(permission);

        IamPermissionEntity created = catalogService.createPermission(MODULE_KEY, "user.read", "Read");

        assertThat(created).isSameAs(permission);
    }

    @Test
    void createPermission_duplicate() {
        when(moduleRepo.findByModuleKey(MODULE_KEY)).thenReturn(Optional.of(module));
        when(permRepo.existsByModule_ModuleKeyAndCode(MODULE_KEY, "user.read")).thenReturn(true);

        assertThatThrownBy(() -> catalogService.createPermission(MODULE_KEY, "user.read", "Read"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("permission already exists");

        verify(permRepo, never()).save(any());
    }

    @Test
    void createPermission_moduleMissing() {
        when(moduleRepo.findByModuleKey(MODULE_KEY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.createPermission(MODULE_KEY, "user.read", "Read"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void listPermissions_delegates() {
        List<IamPermissionEntity> permissions = List.of(new IamPermissionEntity());
        when(permRepo.findByModule_ModuleKey(MODULE_KEY)).thenReturn(permissions);

        assertThat(catalogService.listPermissions(MODULE_KEY)).isSameAs(permissions);
    }

    @Test
    void setTenantModuleEnabled_success_updatesPermVersion() {
        IamTenantModuleEntity existing = new IamTenantModuleEntity();
        when(moduleRepo.findByModuleKey(MODULE_KEY)).thenReturn(Optional.of(module));
        when(tenantModuleRepo.findByTenantIdAndModule_ModuleKey(TENANT_ID, MODULE_KEY)).thenReturn(Optional.of(existing));
        when(tenantModuleRepo.save(existing)).thenReturn(existing);
        when(assignmentRepo.findDistinctSubjectIdsByTenantId(TENANT_ID)).thenReturn(Set.of(1L));
        when(tenantAdminRepo.findDistinctSubjectIdsByTenantId(TENANT_ID)).thenReturn(Set.of(2L));

        IamTenantModuleEntity result = catalogService.setTenantModuleEnabled(TENANT_ID, MODULE_KEY, true);

        assertThat(result).isSameAs(existing);
        verify(permVersionService).incrementForSubject(TENANT_ID, 1L);
        verify(permVersionService).incrementForSubject(TENANT_ID, 2L);
    }

    @Test
    void setTenantModuleEnabled_unknownModule() {
        when(moduleRepo.findByModuleKey(MODULE_KEY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.setTenantModuleEnabled(TENANT_ID, MODULE_KEY, true))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
