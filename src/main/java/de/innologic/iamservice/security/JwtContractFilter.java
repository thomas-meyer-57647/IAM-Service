package de.innologic.iamservice.security;

import de.innologic.iamservice.api.TenantHeaders;
import de.innologic.iamservice.api.error.ApiErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtContractFilter extends OncePerRequestFilter {

    private static final Pattern TENANT_PATH_PATTERN = Pattern.compile("/tenants/([^/]+)");
    private static final List<String> REQUIRED_STRING_CLAIMS = List.of("iss", "sub", "jti", "tenant_id");

    private final ApiErrorWriter apiErrorWriter;

    public JwtContractFilter(ApiErrorWriter apiErrorWriter) {
        this.apiErrorWriter = apiErrorWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        Jwt jwt = jwtAuthenticationToken.getToken();
        String contractError = validateContract(jwt);
        if (contractError != null) {
            apiErrorWriter.write(request, response, HttpStatus.UNAUTHORIZED, contractError);
            return;
        }

        String tokenTenant = jwt.getClaimAsString("tenant_id");
        String tenantMismatchError = validateTenantConsistency(request, tokenTenant);
        if (tenantMismatchError != null) {
            apiErrorWriter.write(request, response, HttpStatus.FORBIDDEN, tenantMismatchError);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String validateContract(Jwt jwt) {
        for (String claim : REQUIRED_STRING_CLAIMS) {
            String value = jwt.getClaimAsString(claim);
            if (value == null || value.isBlank()) {
                return "missing claim: " + claim;
            }
        }

        Instant issuedAt = jwt.getIssuedAt();
        if (issuedAt == null) return "missing claim: iat";
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) return "missing claim: exp";

        List<String> aud = jwt.getAudience();
        if (aud == null || aud.stream().noneMatch("iam-service"::equals)) {
            return "wrong audience";
        }

        return null;
    }

    private String validateTenantConsistency(HttpServletRequest request, String tokenTenant) {
        if (tokenTenant == null || tokenTenant.isBlank()) {
            return "missing claim: tenant_id";
        }

        String headerTenant = request.getHeader(TenantHeaders.TENANT_ID);
        if (headerTenant != null && !headerTenant.isBlank() && !tokenTenant.equals(headerTenant)) {
            return "tenant mismatch";
        }

        String queryTenant = request.getParameter("tenantId");
        if (queryTenant != null && !queryTenant.isBlank() && !tokenTenant.equals(queryTenant)) {
            return "tenant mismatch";
        }

        Matcher matcher = TENANT_PATH_PATTERN.matcher(request.getRequestURI());
        if (matcher.find()) {
            String pathTenant = matcher.group(1);
            if (!tokenTenant.equals(pathTenant)) {
                return "tenant mismatch";
            }
        }

        return null;
    }
}
