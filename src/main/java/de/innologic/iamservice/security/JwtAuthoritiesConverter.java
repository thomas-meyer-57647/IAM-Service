package de.innologic.iamservice.security;
/**
 * Damit du später z. B. ROLE_SYSTEM_ADMIN / ROLE_TENANT_ADMIN nutzen kannst, brauchst du einen Converter.
 * Der hier unterstützt Scopes (scope oder scp) und roles (z. B. roles oder realm_access.roles Keycloak-Style).
 */
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String SCOPE_PREFIX = "SCOPE_";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> authorities = new HashSet<>();

        // 1) Scopes: "scope": "a b c" oder "scp": ["a","b"]
        authorities.addAll(extractScopes(jwt));

        // 2) Roles (verschiedene mögliche Claim-Stellen)
        authorities.addAll(extractRoles(jwt));

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    private Set<String> extractScopes(Jwt jwt) {
        Set<String> result = new HashSet<>();
        Object scope = jwt.getClaims().get("scope");
        if (scope instanceof String s) {
            for (String token : s.split("\\s+")) {
                if (!token.isBlank()) result.add(SCOPE_PREFIX + token.trim());
            }
        }

        Object scp = jwt.getClaims().get("scp");
        if (scp instanceof Collection<?> c) {
            for (Object o : c) {
                if (o != null) result.add(SCOPE_PREFIX + o.toString());
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Jwt jwt) {
        Set<String> result = new HashSet<>();

        // a) simple claim: "roles": ["SYSTEM_ADMIN","TENANT_ADMIN"]
        Object roles = jwt.getClaims().get("roles");
        if (roles instanceof Collection<?> c) {
            for (Object o : c) {
                if (o != null) result.add(toRole(o.toString()));
            }
        }

        // b) Keycloak style: realm_access.roles
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?, ?> map) {
            Object raRoles = map.get("roles");
            if (raRoles instanceof Collection<?> c) {
                for (Object o : c) {
                    if (o != null) result.add(toRole(o.toString()));
                }
            }
        }

        return result;
    }

    private String toRole(String role) {
        String r = role.trim();
        if (r.isEmpty()) return r;
        // Spring Security Konvention: ROLE_
        return r.startsWith(ROLE_PREFIX) ? r : ROLE_PREFIX + r;
    }
}
