package de.innologic.iamservice.system;

import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.api.SystemAdminController;
import de.innologic.iamservice.api.error.ApiErrorWriter;
import de.innologic.iamservice.catalog.service.CatalogService;
import de.innologic.iamservice.config.SecurityConfig;
import de.innologic.iamservice.config.SecurityErrorHandlers;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.permission.entity.IamPermissionEntity;
import de.innologic.iamservice.module.entity.IamModuleEntity;
import de.innologic.iamservice.tenant.entity.IamTenantModuleEntity;
import de.innologic.iamservice.security.IamAuthorizationService;
import de.innologic.iamservice.security.JwtContractFilter;
import de.innologic.iamservice.test.TestSecurityBeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SystemAdminController.class)
@Import({SecurityConfig.class, TestSecurityBeans.class, ApiErrorWriter.class, SecurityErrorHandlers.class, JwtContractFilter.class})
class SystemAdminControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CatalogService catalogService;

    @MockBean
    AdminService adminService;

    @MockBean(name = "iamAuthz")
    IamAuthorizationService iamAuthorizationService;

    @BeforeEach
    void setupAuthz() {
        when(iamAuthorizationService.isSystemAdmin(any())).thenReturn(true);
    }

    private RequestPostProcessor validJwt() {
        return jwt().jwt(jwt -> jwt
                .claim("iss", "https://auth.example.com")
                .claim("aud", List.of("iam-service"))
                .claim("jti", "jti-system")
                .claim("tenant_id", "COMPANY-1000")
                .claim("subject_type", "USER")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600)));
    }

    @Test
    void createModule_returnsCreatedModule() throws Exception {
        IamModuleEntity module = mock(IamModuleEntity.class);
        when(module.getId()).thenReturn(1L);
        when(module.getModuleKey()).thenReturn("user");
        when(module.getName()).thenReturn("User");
        when(module.getDescription()).thenReturn("User module");
        when(module.isActive()).thenReturn(true);
        when(catalogService.createModule(eq("user"), eq("User"), eq("User module"))).thenReturn(module);

        mockMvc.perform(post("/v1/modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"moduleKey\":\"user\",\"name\":\"User\",\"description\":\"User module\"}")
                        .with(validJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.moduleKey").value("user"))
                .andExpect(jsonPath("$.name").value("User"));
    }

    @Test
    void createModule_missingJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/v1/modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"moduleKey\":\"user\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listModules_returnsModuleList() throws Exception {
        IamModuleEntity module = mock(IamModuleEntity.class);
        when(module.getId()).thenReturn(2L);
        when(module.getModuleKey()).thenReturn("user");
        when(module.getName()).thenReturn("User");
        when(module.getDescription()).thenReturn("desc");
        when(module.isActive()).thenReturn(true);
        when(catalogService.listModules()).thenReturn(List.of(module));

        mockMvc.perform(get("/v1/modules").with(validJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].moduleKey").value("user"));
    }

    @Test
    void listModules_forbiddenWithoutSystemAdmin() throws Exception {
        when(iamAuthorizationService.isSystemAdmin(any())).thenReturn(false);

        mockMvc.perform(get("/v1/modules").with(validJwt()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.path").value("/v1/modules"));
    }

    @Test
    void createPermission_returnsPermission() throws Exception {
        IamModuleEntity module = mock(IamModuleEntity.class);
        IamPermissionEntity permission = mock(IamPermissionEntity.class);
        when(permission.getId()).thenReturn(5L);
        when(permission.getModule()).thenReturn(module);
        when(permission.getCode()).thenReturn("user.read");
        when(permission.getDescription()).thenReturn("Read users");
        when(permission.isActive()).thenReturn(true);
        when(catalogService.createPermission(eq("user"), eq("user.read"), eq("Read users"))).thenReturn(permission);

        mockMvc.perform(post("/v1/modules/user/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"user.read\",\"description\":\"Read users\"}")
                        .with(validJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("user.read"));
    }

    @Test
    void createPermission_invalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/v1/modules/user/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json")
                        .with(validJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/v1/modules/user/permissions"));
    }

    @Test
    void listPermissions_returnsList() throws Exception {
        IamPermissionEntity permission = mock(IamPermissionEntity.class);
        when(permission.getId()).thenReturn(8L);
        when(permission.getCode()).thenReturn("user.read");
        when(permission.getDescription()).thenReturn("desc");
        when(permission.isActive()).thenReturn(true);
        when(catalogService.listPermissions("user")).thenReturn(List.of(permission));

        mockMvc.perform(get("/v1/modules/user/permissions").with(validJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("user.read"));
    }

    @Test
    void setTenantModuleEnabled_requiresHeader() throws Exception {
        mockMvc.perform(put("/v1/tenant/modules/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}")
                        .with(validJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/v1/tenant/modules/user"));
    }

    @Test
    void setTenantModuleEnabled_callsService() throws Exception {
        IamTenantModuleEntity tm = mock(IamTenantModuleEntity.class);
        when(catalogService.setTenantModuleEnabled(eq("COMPANY-1000"), eq("user"), eq(true))).thenReturn(tm);

        mockMvc.perform(put("/v1/tenant/modules/user")
                        .header("X-Tenant-Id", "COMPANY-1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":true}")
                        .with(validJwt()))
                .andExpect(status().isOk());

        verify(catalogService).setTenantModuleEnabled("COMPANY-1000", "user", true);
    }

    @Test
    void addSystemAdmin_success() throws Exception {
        mockMvc.perform(post("/v1/system/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":\"SYS-1\",\"subjectType\":\"SERVICE\"}")
                        .with(validJwt()))
                .andExpect(status().isOk());

        verify(adminService).addSystemAdmin("SYS-1", SubjectType.SERVICE);
    }

    @Test
    void addSystemAdmin_missingJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/v1/system/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":\"SYS-1\",\"subjectType\":\"SERVICE\"}"))
                .andExpect(status().isUnauthorized());
    }
}
