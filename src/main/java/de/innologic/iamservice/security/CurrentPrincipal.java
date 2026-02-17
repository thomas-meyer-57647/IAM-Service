package de.innologic.iamservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public final class CurrentPrincipal {

    private CurrentPrincipal() {}

    public static Optional<Jwt> jwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Optional.empty();
        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) return Optional.of(jwt);
        return Optional.empty();
    }

    /** subjectId (JWT "sub") */
    public static Optional<String> subjectId() {
        return jwt().map(Jwt::getSubject);
    }

    /** tenantId (V1.1 Contract: Claim "tenant_id", Legacy-Fallback "tenantId") */
    public static Optional<String> tenantId() {
        return jwt().map(j -> {
            String tenant = j.getClaimAsString("tenant_id");
            if (tenant == null || tenant.isBlank()) {
                tenant = j.getClaimAsString("tenantId");
            }
            return tenant;
        }).filter(t -> t != null && !t.isBlank());
    }

    /** optional: subjectType (USER|SERVICE) */
    public static Optional<String> subjectType() {
        return jwt().map(j -> j.getClaimAsString("subjectType"));
    }
}
