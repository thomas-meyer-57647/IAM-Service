package de.innologic.iamservice.api;

import de.innologic.iamservice.access.service.AccessQueryService;
import de.innologic.iamservice.api.dto.ModuleDtos;
import de.innologic.iamservice.domain.SubjectType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/access")
public class AccessController {

    private final AccessQueryService accessQueryService;

    public AccessController(AccessQueryService accessQueryService) {
        this.accessQueryService = accessQueryService;
    }

    @PreAuthorize("@iamAuthz.canQueryAccess(authentication, #tenantId, #subjectId)")
    @GetMapping("/tenants/{tenantId}/subjects/{subjectId}/modules/{moduleKey}")
    public ModuleDtos.AccessResponse permissions(@PathVariable String tenantId,
                                                 @PathVariable String subjectId,
                                                 @PathVariable String moduleKey,
                                                 @RequestParam(defaultValue = "USER") String subjectType) {

        var perms = accessQueryService.getPermissions(
                tenantId,
                subjectId,
                SubjectType.valueOf(subjectType),
                moduleKey
        );

        return new ModuleDtos.AccessResponse(tenantId, subjectId, moduleKey, perms);
    }
}
