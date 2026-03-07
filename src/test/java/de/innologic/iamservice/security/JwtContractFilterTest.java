package de.innologic.iamservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.innologic.iamservice.api.error.ApiErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class JwtContractFilterTest {

    private JwtContractFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtContractFilter(new ApiErrorWriter(new ObjectMapper().registerModule(new JavaTimeModule())));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validJwtPassesThrough() throws ServletException, IOException {
        Jwt jwt = buildJwt(builder -> {
        });

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/access/subjects/USR-1/modules/user");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean();
        FilterChain chain = capturingChain(invoked);

        setAuthentication(jwt);

        filter.doFilterInternal(request, response, chain);

        assertThat(invoked).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void missingIssuerReturnsUnauthorized() throws ServletException, IOException {
        Jwt jwt = buildJwt(builder -> builder.claim("iss", null));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/access/subjects/USR-1/modules/user");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean();
        FilterChain chain = capturingChain(invoked);

        setAuthentication(jwt);

        filter.doFilterInternal(request, response, chain);

        assertThat(invoked).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("missing claim: iss");
    }

    @Test
    void wrongAudienceReturnsUnauthorized() throws ServletException, IOException {
        Jwt jwt = buildJwt(builder -> builder.audience(List.of("other-service")));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/access/subjects/USR-1/modules/user");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean();
        FilterChain chain = capturingChain(invoked);

        setAuthentication(jwt);

        filter.doFilterInternal(request, response, chain);

        assertThat(invoked).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("wrong audience");
    }

    @Test
    void invalidSubjectTypeReturnsUnauthorized() throws ServletException, IOException {
        Jwt jwt = buildJwt(builder -> builder.claim("subject_type", "ADMIN"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/access/subjects/USR-1/modules/user");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean();
        FilterChain chain = capturingChain(invoked);

        setAuthentication(jwt);

        filter.doFilterInternal(request, response, chain);

        assertThat(invoked).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("invalid subject_type");
    }

    @Test
    void missingTenantIdReturnsUnauthorized() throws ServletException, IOException {
        Jwt jwt = buildJwt(builder -> builder.claim("tenant_id", null));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/access/subjects/USR-1/modules/user");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean();
        FilterChain chain = capturingChain(invoked);

        setAuthentication(jwt);

        filter.doFilterInternal(request, response, chain);

        assertThat(invoked).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("missing claim: tenant_id");
    }

    @Test
    void tenantHeaderMismatchReturnsForbidden() throws ServletException, IOException {
        Jwt jwt = buildJwt(builder -> builder.claim("tenant_id", "tenantA"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/access/tenants/tenantB/subjects/USR-1/modules/user");
        request.addHeader("X-Tenant-Id", "tenantB");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean invoked = new AtomicBoolean();
        FilterChain chain = capturingChain(invoked);

        setAuthentication(jwt);

        filter.doFilterInternal(request, response, chain);

        assertThat(invoked).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("tenant mismatch");
    }

    private void setAuthentication(Jwt jwt) {
        JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Jwt buildJwt(Consumer<Jwt.Builder> customizer) {
        Instant now = Instant.now();
        Jwt.Builder builder = Jwt.withTokenValue("jwt-token")
                .header("alg", "none")
                .claim("iss", "https://auth.example.com")
                .claim("sub", "USR-1")
                .claim("jti", "jti-1")
                .claim("tenant_id", "tenantA")
                .claim("subject_type", "USER")
                .audience(List.of("iam-service"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600));

        customizer.accept(builder);
        return builder.build();
    }

    private FilterChain capturingChain(AtomicBoolean invoked) {
        return new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                invoked.set(true);
            }
        };
    }
}
