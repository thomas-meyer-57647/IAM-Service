package de.innologic.iamservice.access;

import de.innologic.iamservice.access.service.AccessQueryService;
import de.innologic.iamservice.api.AccessController;
import de.innologic.iamservice.api.error.ApiErrorWriter;
import de.innologic.iamservice.config.SecurityConfig;
import de.innologic.iamservice.config.SecurityErrorHandlers;
import de.innologic.iamservice.security.JwtContractFilter;
import de.innologic.iamservice.security.IamAuthorizationService;
import de.innologic.iamservice.test.TestSecurityBeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccessController.class)
@Import({SecurityConfig.class, TestSecurityBeans.class, ApiErrorWriter.class, SecurityErrorHandlers.class, JwtContractFilter.class})
class AccessControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccessQueryService accessQueryService;

    @MockBean(name = "iamAuthz")
    IamAuthorizationService iamAuthorizationService; // iamAuthz

    @BeforeEach
    void stubAuthz() {
        when(iamAuthorizationService.canQueryAccess(any(), anyString(), anyString())).thenReturn(true);
        when(iamAuthorizationService.canQueryAccess(any(), anyString())).thenReturn(true);
    }

    @Test
    void returnsPermissionsForModule() throws Exception {
        when(accessQueryService.getAccess(eq("tenantA"), eq("user123"), any(), eq("timeentry")))
                .thenReturn(new AccessQueryService.AccessQueryResult(
                        true,
                        java.util.List.of("timeentry.read", "timeentry.write"),
                        42L
                ));

        mockMvc.perform(get("/v1/access/tenants/{tenantId}/subjects/{subjectId}/modules/{moduleKey}",
                        "tenantA", "user123", "timeentry")
                        .param("subjectType", "USER")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("iss", "https://auth.example.com")
                                .claim("aud", java.util.List.of("iam-service"))
                                .claim("jti", "jti-1")
                                .claim("subject_type", "USER")
                                .claim("tenant_id", "tenantA")
                                .issuedAt(java.time.Instant.now())
                                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.permissions[0]").value("timeentry.read"))
                .andExpect(jsonPath("$.permissions[1]").value("timeentry.write"))
                .andExpect(jsonPath("$.permVersion").value(42));
    }

    @Test
    void missingMandatoryClaims_returns401() throws Exception {
        mockMvc.perform(get("/v1/access/tenants/{tenantId}/subjects/{subjectId}/modules/{moduleKey}",
                        "tenantA", "user123", "timeentry")
                        .with(jwt().jwt(jwt -> jwt.claim("sub", "user123"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void missingTenantIdClaim_returns401() throws Exception {
        mockMvc.perform(get("/v1/access/subjects/{subjectId}/modules/{moduleKey}", "user123", "timeentry")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("iss", "https://auth.example.com")
                                .claim("aud", java.util.List.of("iam-service"))
                                .claim("jti", "jti-3")
                                .claim("subject_type", "USER")
                                .issuedAt(java.time.Instant.now())
                                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void missingAudience_returns401() throws Exception {
        mockMvc.perform(get("/v1/access/subjects/{subjectId}/modules/{moduleKey}", "user123", "timeentry")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("iss", "https://auth.example.com")
                                .claim("jti", "jti-4")
                                .claim("tenant_id", "tenantA")
                                .claim("subject_type", "USER")
                                .issuedAt(java.time.Instant.now())
                                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void missingJti_returns401() throws Exception {
        mockMvc.perform(get("/v1/access/subjects/{subjectId}/modules/{moduleKey}", "user123", "timeentry")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("iss", "https://auth.example.com")
                                .claim("aud", java.util.List.of("iam-service"))
                                .claim("tenant_id", "tenantA")
                                .claim("subject_type", "USER")
                                .issuedAt(java.time.Instant.now())
                                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void missingSubjectType_returns401() throws Exception {
        mockMvc.perform(get("/v1/access/subjects/{subjectId}/modules/{moduleKey}", "user123", "timeentry")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("iss", "https://auth.example.com")
                                .claim("aud", java.util.List.of("iam-service"))
                                .claim("jti", "jti-6")
                                .claim("tenant_id", "tenantA")
                                .issuedAt(java.time.Instant.now())
                                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void invalidSubjectType_returns401() throws Exception {
        mockMvc.perform(get("/v1/access/subjects/{subjectId}/modules/{moduleKey}", "user123", "timeentry")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("iss", "https://auth.example.com")
                                .claim("aud", java.util.List.of("iam-service"))
                                .claim("jti", "jti-7")
                                .claim("tenant_id", "tenantA")
                                .claim("subject_type", "ADMIN")
                                .issuedAt(java.time.Instant.now())
                                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void missingIat_returns401() throws Exception {
        mockMvc.perform(get("/v1/access/subjects/{subjectId}/modules/{moduleKey}", "user123", "timeentry")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("iss", "https://auth.example.com")
                                .claim("aud", java.util.List.of("iam-service"))
                                .claim("jti", "jti-5")
                                .claim("tenant_id", "tenantA")
                                .claim("subject_type", "USER")
                                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void tenantMismatch_returns403() throws Exception {
        mockMvc.perform(get("/v1/access/tenants/{tenantId}/subjects/{subjectId}/modules/{moduleKey}",
                        "tenantB", "user123", "timeentry")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("iss", "https://auth.example.com")
                                .claim("aud", java.util.List.of("iam-service"))
                                .claim("jti", "jti-2")
                                .claim("tenant_id", "tenantA")
                                .claim("subject_type", "USER")
                                .issuedAt(java.time.Instant.now())
                                .expiresAt(java.time.Instant.now().plusSeconds(3600))
                        )))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
