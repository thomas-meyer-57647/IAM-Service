package de.innologic.iamservice.tenant;

import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.api.TenantAdminController;
import de.innologic.iamservice.api.TenantHeaders;
import de.innologic.iamservice.api.error.ApiErrorWriter;
import de.innologic.iamservice.config.SecurityConfig;
import de.innologic.iamservice.config.SecurityErrorHandlers;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.role.entity.IamRoleEntity;
import de.innologic.iamservice.role.service.RoleService;
import de.innologic.iamservice.security.IamAuthorizationService;
import de.innologic.iamservice.security.JwtContractFilter;
import de.innologic.iamservice.test.TestSecurityBeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TenantAdminController.class)
@Import({SecurityConfig.class, TestSecurityBeans.class, ApiErrorWriter.class, SecurityErrorHandlers.class, JwtContractFilter.class})
class TenantAdminControllerWebMvcTest {

    private static final String TENANT_ID_VALUE = "COMPANY-100";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RoleService roleService;

    @MockBean
    AdminService adminService;

    @MockBean(name = "iamAuthz")
    IamAuthorizationService iamAuthorizationService;

    @BeforeEach
    void setupAuthz() {
        when(iamAuthorizationService.isSystemAdmin(any())).thenReturn(false);
        when(iamAuthorizationService.isTenantAdmin(any(), anyString())).thenReturn(true);
    }

    private RequestPostProcessor validJwt() {
        return jwt().jwt(jwt -> jwt
                .claim("iss", "https://auth.example.com")
                .claim("aud", List.of("iam-service"))
                .claim("jti", "jti-tenant")
                .claim("tenant_id", TENANT_ID_VALUE)
                .claim("subject_type", "USER")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600)));
    }

    @Test
    void createRole_returnsCreatedRole() throws Exception {
        IamRoleEntity role = mock(IamRoleEntity.class);
        when(role.getId()).thenReturn(12L);
        when(role.getTenantId()).thenReturn(TENANT_ID_VALUE);
        when(role.getName()).thenReturn("Tenant Lead");
        when(role.getDescription()).thenReturn("Manages the tenant");
        when(roleService.createRole(eq(TENANT_ID_VALUE), eq("Tenant Lead"), eq("Manages the tenant")))
                .thenReturn(role);

        mockMvc.perform(post("/v1/tenant/roles")
                        .header(TenantHeaders.TENANT_ID, TENANT_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tenant Lead\",\"description\":\"Manages the tenant\"}")
                        .with(validJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.tenantId").value(TENANT_ID_VALUE))
                .andExpect(jsonPath("$.name").value("Tenant Lead"));
    }

    @Test
    void createRole_missingTenantHeader_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/v1/tenant/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tenant Lead\"}")
                        .with(validJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/v1/tenant/roles"));
    }

    @Test
    void listRoles_returnsRoles() throws Exception {
        IamRoleEntity role = mock(IamRoleEntity.class);
        when(role.getId()).thenReturn(3L);
        when(role.getTenantId()).thenReturn(TENANT_ID_VALUE);
        when(role.getName()).thenReturn("Tenant Support");
        when(role.getDescription()).thenReturn("Handles tenant support");
        when(role.isActive()).thenReturn(true);
        when(roleService.listRoles(TENANT_ID_VALUE)).thenReturn(List.of(role));

        mockMvc.perform(get("/v1/tenant/roles")
                        .header(TenantHeaders.TENANT_ID, TENANT_ID_VALUE)
                        .with(validJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Tenant Support"))
                .andExpect(jsonPath("$[0].tenantId").value(TENANT_ID_VALUE));
    }

    @Test
    void listRoles_forbiddenWithoutTenantAdmin() throws Exception {
        when(iamAuthorizationService.isTenantAdmin(any(), anyString())).thenReturn(false);

        mockMvc.perform(get("/v1/tenant/roles")
                        .header(TenantHeaders.TENANT_ID, TENANT_ID_VALUE)
                        .with(validJwt()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.path").value("/v1/tenant/roles"));
    }

    @Test
    void setRolePermissions_returnsOk() throws Exception {
        mockMvc.perform(put("/v1/tenant/roles/15/permissions")
                        .header(TenantHeaders.TENANT_ID, TENANT_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissionCodes\":[\"perm.read\"]}")
                        .with(validJwt()))
                .andExpect(status().isOk());

        verify(roleService).setRolePermissions(eq(TENANT_ID_VALUE), eq(15L), eq(List.of("perm.read")));
    }

    @Test
    void setRolePermissions_invalidJson_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/v1/tenant/roles/15/permissions")
                        .header(TenantHeaders.TENANT_ID, TENANT_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid")
                        .with(validJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/v1/tenant/roles/15/permissions"));
    }

    @Test
    void assignRole_returnsOk() throws Exception {
        mockMvc.perform(post("/v1/tenant/assignments")
                        .header(TenantHeaders.TENANT_ID, TENANT_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":\"USR-1\",\"subjectType\":\"USER\",\"roleId\":23}")
                        .with(validJwt()))
                .andExpect(status().isOk());

        verify(roleService).assignRole(TENANT_ID_VALUE, "USR-1", SubjectType.USER, 23L);
    }

    @Test
    void assignRole_missingTenantHeader_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/v1/tenant/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":\"USR-1\",\"subjectType\":\"USER\",\"roleId\":23}")
                        .with(validJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/v1/tenant/assignments"));
    }

    @Test
    void addTenantAdmin_returnsOk() throws Exception {
        mockMvc.perform(put("/v1/tenant/admins")
                        .header(TenantHeaders.TENANT_ID, TENANT_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":\"USR-1\",\"subjectType\":\"USER\"}")
                        .with(validJwt()))
                .andExpect(status().isOk());

        verify(adminService).addTenantAdmin(TENANT_ID_VALUE, "USR-1", SubjectType.USER);
    }

    @Test
    void addTenantAdmin_missingTenantHeader_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/v1/tenant/admins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":\"USR-1\",\"subjectType\":\"USER\"}")
                        .with(validJwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/v1/tenant/admins"));
    }

    @Test
    void removeTenantAdmin_returnsOk() throws Exception {
        mockMvc.perform(delete("/v1/tenant/admins")
                        .header(TenantHeaders.TENANT_ID, TENANT_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":\"USR-1\",\"subjectType\":\"USER\"}")
                        .with(validJwt()))
                .andExpect(status().isOk());

        verify(adminService).removeTenantAdmin(TENANT_ID_VALUE, "USR-1", SubjectType.USER);
    }

    @Test
    void removeTenantAdmin_lastAdmin_returnsConflict() throws Exception {
        doThrow(new IllegalStateException("tenant must have at least one admin"))
                .when(adminService).removeTenantAdmin(TENANT_ID_VALUE, "USR-1", SubjectType.USER);

        mockMvc.perform(delete("/v1/tenant/admins")
                        .header(TenantHeaders.TENANT_ID, TENANT_ID_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subjectId\":\"USR-1\",\"subjectType\":\"USER\"}")
                        .with(validJwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/v1/tenant/admins"));
    }
}
