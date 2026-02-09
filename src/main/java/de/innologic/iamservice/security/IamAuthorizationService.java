package de.innologic.iamservice.security;

import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.subject.entity.IamSubjectEntity;
import de.innologic.iamservice.subject.repo.IamSubjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("iamAuthorizationService")
public class IamAuthorizationService {

    private final IamSubjectRepository subjectRepo;
    private final AdminService adminService;

    public IamAuthorizationService(IamSubjectRepository subjectRepo, AdminService adminService) {
        this.subjectRepo = subjectRepo;
        this.adminService = adminService;
    }

    public boolean isSystemAdmin(Authentication auth) {
        return resolveSubject(auth)
                .map(subjectPk -> adminService.isSystemAdmin(subjectPk))
                .orElse(false);
    }

    public boolean isTenantAdmin(Authentication auth, String tenantId) {
        // SystemAdmin darf alles
        if (isSystemAdmin(auth)) return true;

        return resolveSubject(auth)
                .map(subjectPk -> adminService.isTenantAdmin(tenantId, subjectPk))
                .orElse(false);
    }

    /**
     * Zugriff auf /v1/access...:
     * - System Admin -> ok
     * - Tenant Admin -> ok
     * - Self (subjectId == token.sub) -> ok
     */
    public boolean canQueryAccess(Authentication auth, String tenantId, String subjectId) {
        if (isSystemAdmin(auth)) return true;

        String tokenSubjectId = resolveSubjectId(auth).orElse(null);
        if (tokenSubjectId != null && tokenSubjectId.equals(subjectId)) return true;

        return isTenantAdmin(auth, tenantId);
    }

    private Optional<Long> resolveSubject(Authentication auth) {
        String subjectId = resolveSubjectId(auth).orElse(null);
        if (subjectId == null || subjectId.isBlank()) return Optional.empty();

        SubjectType type = resolveSubjectType(auth);

        IamSubjectEntity s = subjectRepo.findBySubjectIdAndSubjectType(subjectId, type).orElse(null);
        return s == null ? Optional.empty() : Optional.of(s.getId());
    }

    private Optional<String> resolveSubjectId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();

        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return Optional.ofNullable(jwt.getSubject()).filter(x -> !x.isBlank());
        }
        return Optional.ofNullable(auth.getName()).filter(x -> !x.isBlank());
    }

    private SubjectType resolveSubjectType(Authentication auth) {
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (principal instanceof Jwt jwt) {
            Object claim = jwt.getClaims().get("subject_type");
            if (claim != null) {
                try {
                    return SubjectType.valueOf(claim.toString().trim().toUpperCase());
                } catch (Exception ignore) {
                    // fallback
                }
            }
        }
        return SubjectType.USER;
    }
}
