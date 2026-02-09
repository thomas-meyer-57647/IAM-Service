package de.innologic.iamservice.access;

import de.innologic.iamservice.access.service.AccessQueryService;
import de.innologic.iamservice.api.AccessController;
import de.innologic.iamservice.config.SecurityConfig;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.security.IamAuthorizationService;
import de.innologic.iamservice.security.IamAuthz;
import de.innologic.iamservice.test.TestSecurityBeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccessController.class)
@Import({SecurityConfig.class, TestSecurityBeans.class})
class AccessControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AccessQueryService accessQueryService;

    @MockBean
    IamAuthorizationService iamAuthorizationService; // iamAuthz

    @MockBean(name = "iamAuthz")
    private IamAuthz iamAuthz;

    @BeforeEach
    void stubAuthz() {
        when(iamAuthz.canQueryAccess(any(), anyString(), anyString())).thenReturn(true);
    }

    @Test
    void returnsPermissionsForModule() throws Exception {
        when(iamAuthorizationService.canQueryAccess(any(), eq("tenantA"), eq("user123"))).thenReturn(true);

        when(accessQueryService.getPermissions("tenantA", "user123", SubjectType.USER, "timeentry"))
                .thenReturn(List.of("timeentry.read", "timeentry.write"));

        mockMvc.perform(get("/v1/access/tenants/{tenantId}/subjects/{subjectId}/modules/{moduleKey}",
                        "tenantA", "user123", "timeentry")
                        .param("subjectType", "USER")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value("tenantA"))
                .andExpect(jsonPath("$.subjectId").value("user123"))
                .andExpect(jsonPath("$.moduleKey").value("timeentry"))
                .andExpect(jsonPath("$.permissions[0]").value("timeentry.read"))
                .andExpect(jsonPath("$.permissions[1]").value("timeentry.write"));
    }
}
