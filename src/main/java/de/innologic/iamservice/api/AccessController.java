package de.innologic.iamservice.api;

import de.innologic.iamservice.access.service.AccessQueryService;
import de.innologic.iamservice.api.dto.ModuleDtos;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.security.CurrentPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/access")
@Tag(name = "Access", description = "Endpoints to query effective permissions for subjects and modules")
public class AccessController {

    private final AccessQueryService accessQueryService;

    public AccessController(AccessQueryService accessQueryService) {
        this.accessQueryService = accessQueryService;
    }

    @Operation(
            summary = "Get effective permissions for a subject",
            description = "Returns whether the module is enabled for the tenant, the granted permissions and the permVersion for the given subject and module.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Effective permissions returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT or required claims"),
            @ApiResponse(responseCode = "403", description = "Tenant mismatch or insufficient rights"),
            @ApiResponse(responseCode = "404", description = "Subject or module not found"),
            @ApiResponse(responseCode = "500", description = "Internal error")
    })
    @PreAuthorize("@iamAuthz.canQueryAccess(authentication, #subjectId)")
    @GetMapping("/subjects/{subjectId}/modules/{moduleKey}")
    public ModuleDtos.AccessResponse canonicalPermissions(
            @Parameter(in = ParameterIn.PATH, description = "Subject identifier for which access should be checked", example = "USR-10001", required = true)
            @PathVariable String subjectId,
            @Parameter(in = ParameterIn.PATH, description = "Module key representing the area to inspect", example = "user", required = true)
            @PathVariable String moduleKey,
            @Parameter(in = ParameterIn.QUERY, description = "Subject type, either USER or SERVICE (defaults to USER)", example = "USER", required = false)
            @RequestParam(defaultValue = "USER") String subjectType) {
        String tenantId = CurrentPrincipal.tenantId()
                .orElseThrow(() -> new IllegalArgumentException("missing claim: tenant_id"));
        return resolvePermissions(tenantId, subjectId, moduleKey, subjectType);
    }

    @Operation(
            summary = "Legacy access query",
            description = "Deprecated endpoint that exposes the same information as the canonical path.",
            security = @SecurityRequirement(name = "bearerAuth"),
            deprecated = true
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Effective permissions returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT or required claims"),
            @ApiResponse(responseCode = "403", description = "Tenant mismatch or insufficient rights"),
            @ApiResponse(responseCode = "404", description = "Subject or module not found"),
            @ApiResponse(responseCode = "500", description = "Internal error")
    })
    @PreAuthorize("@iamAuthz.canQueryAccess(authentication, #tenantId, #subjectId)")
    @GetMapping("/tenants/{tenantId}/subjects/{subjectId}/modules/{moduleKey}")
    public ModuleDtos.AccessResponse legacyPermissions(
            @Parameter(in = ParameterIn.PATH, description = "Tenant identifier as part of the legacy path", example = "COMPANY-1000", required = true)
            @PathVariable String tenantId,
            @Parameter(in = ParameterIn.PATH, description = "Subject identifier for which access should be checked", example = "USR-10001", required = true)
            @PathVariable String subjectId,
            @Parameter(in = ParameterIn.PATH, description = "Module key representing the area to inspect", example = "user", required = true)
            @PathVariable String moduleKey,
            @Parameter(in = ParameterIn.QUERY, description = "Subject type, either USER or SERVICE (defaults to USER)", example = "USER", required = false)
            @RequestParam(defaultValue = "USER") String subjectType) {
        return resolvePermissions(tenantId, subjectId, moduleKey, subjectType);
    }

    private ModuleDtos.AccessResponse resolvePermissions(String tenantId, String subjectId, String moduleKey, String subjectType) {
        var access = accessQueryService.getAccess(tenantId, subjectId, SubjectType.valueOf(subjectType), moduleKey);
        return new ModuleDtos.AccessResponse(access.enabled(), access.permissions(), access.permVersion());
    }
}
