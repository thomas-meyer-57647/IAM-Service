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

    /** optional: tenantId (z.B. Claim "tenantId") */
    public static Optional<String> tenantId() {
        return jwt().map(j -> j.getClaimAsString("tenantId"));
    }

    /** optional: subjectType (USER|SERVICE) */
    public static Optional<String> subjectType() {
        return jwt().map(j -> j.getClaimAsString("subjectType"));
    }
}
