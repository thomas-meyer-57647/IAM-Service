package de.innologic.iamservice.api;

import de.innologic.iamservice.access.service.AccessQueryService;
import de.innologic.iamservice.api.dto.ModuleDtos;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.security.CurrentPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/access")
public class AccessController {

    private final AccessQueryService accessQueryService;

    public AccessController(AccessQueryService accessQueryService) {
        this.accessQueryService = accessQueryService;
    }

    @PreAuthorize("@iamAuthz.canQueryAccess(authentication, #subjectId)")
    @GetMapping("/subjects/{subjectId}/modules/{moduleKey}")
    public ModuleDtos.AccessResponse canonicalPermissions(@PathVariable String subjectId,
                                                          @PathVariable String moduleKey,
                                                          @RequestParam(defaultValue = "USER") String subjectType) {
        String tenantId = CurrentPrincipal.tenantId()
                .orElseThrow(() -> new IllegalArgumentException("missing claim: tenant_id"));
        return resolvePermissions(tenantId, subjectId, moduleKey, subjectType);
    }

    @Operation(deprecated = true)
    @PreAuthorize("@iamAuthz.canQueryAccess(authentication, #tenantId, #subjectId)")
    @GetMapping("/tenants/{tenantId}/subjects/{subjectId}/modules/{moduleKey}")
    public ModuleDtos.AccessResponse legacyPermissions(@PathVariable String tenantId,
                                                       @PathVariable String subjectId,
                                                       @PathVariable String moduleKey,
                                                       @RequestParam(defaultValue = "USER") String subjectType) {
        return resolvePermissions(tenantId, subjectId, moduleKey, subjectType);
    }

    private ModuleDtos.AccessResponse resolvePermissions(String tenantId, String subjectId, String moduleKey, String subjectType) {
        var access = accessQueryService.getAccess(tenantId, subjectId, SubjectType.valueOf(subjectType), moduleKey);
        return new ModuleDtos.AccessResponse(access.enabled(), access.permissions(), access.permVersion());
    }
}
