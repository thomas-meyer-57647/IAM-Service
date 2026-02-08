package de.innologic.iamservice.persistence.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

/**
 * Liefert den "aktuellen Benutzer" für @CreatedBy/@LastModifiedBy.
 * - JWT vorhanden -> jwt.getSubject()
 * - sonst -> auth.getName()
 * - fallback -> "system"
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.of("system");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            return Optional.ofNullable(sub).filter(s -> !s.isBlank()).or(() -> Optional.of(auth.getName()));
        }

        String name = auth.getName();
        return Optional.ofNullable(name).filter(s -> !s.isBlank()).or(() -> Optional.of("system"));
    }
}
