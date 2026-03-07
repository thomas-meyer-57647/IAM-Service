package de.innologic.iamservice.api;

import de.innologic.iamservice.api.dto.ModuleDtos;
import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.catalog.service.CatalogService;
import de.innologic.iamservice.domain.SubjectType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.innologic.iamservice.api.TenantHeaders.TENANT_ID;

@RestController
@RequestMapping("/v1")
@Tag(name = "System Administration", description = "Endpoints for module catalog management, permissions, tenant activation and system admins")
public class SystemAdminController {

    private final CatalogService catalogService;
    private final AdminService adminService;

    public SystemAdminController(CatalogService catalogService, AdminService adminService) {
        this.catalogService = catalogService;
        this.adminService = adminService;
    }

    @Operation(
            summary = "Create a new module",
            description = "System admins can add catalog modules with metadata.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Module created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "System admin rights required"),
            @ApiResponse(responseCode = "409", description = "Module already exists")
    })
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/modules")
    public ModuleDtos.ModuleResponse createModule(@RequestBody ModuleDtos.CreateModuleRequest req) {
        var m = catalogService.createModule(req.moduleKey(), req.name(), req.description());
        return new ModuleDtos.ModuleResponse(m.getId(), m.getModuleKey(), m.getName(), m.getDescription(), m.isActive());
    }

    @Operation(
            summary = "Legacy create module",
            description = "Deprecated module creation path for older clients.",
            security = @SecurityRequirement(name = "bearerAuth"),
            deprecated = true
    )
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/system/modules")
    public ModuleDtos.ModuleResponse createModuleLegacy(@RequestBody ModuleDtos.CreateModuleRequest req) {
        return createModule(req);
    }

    @Operation(
            summary = "List available modules",
            description = "Returns the catalog of modules that system admins manage.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Module catalog returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "System admin rights required")
    })
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @GetMapping("/modules")
    public List<ModuleDtos.ModuleResponse> listModules() {
        return catalogService.listModules().stream()
                .map(m -> new ModuleDtos.ModuleResponse(m.getId(), m.getModuleKey(), m.getName(), m.getDescription(), m.isActive()))
                .toList();
    }

    @Operation(
            summary = "Legacy list modules",
            description = "Deprecated module listing path.",
            security = @SecurityRequirement(name = "bearerAuth"),
            deprecated = true
    )
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @GetMapping("/system/modules")
    public List<ModuleDtos.ModuleResponse> listModulesLegacy() {
        return listModules();
    }

    @Operation(
            summary = "Create a new permission",
            description = "Attaches a permission to an existing module.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "System admin rights required"),
            @ApiResponse(responseCode = "404", description = "Module not found"),
            @ApiResponse(responseCode = "409", description = "Permission already exists")
    })
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/modules/{moduleKey}/permissions")
    public ModuleDtos.PermissionResponse createPermission(
            @Parameter(in = ParameterIn.PATH, description = "Module key as declared in the catalog", example = "user", required = true)
            @PathVariable String moduleKey,
            @RequestBody ModuleDtos.CreatePermissionRequest req) {
        var p = catalogService.createPermission(moduleKey, req.code(), req.description());
        return new ModuleDtos.PermissionResponse(p.getId(), moduleKey, p.getCode(), p.getDescription(), p.isActive());
    }

    @Operation(
            summary = "Legacy create permission",
            description = "Deprecated permission creation endpoint.",
            security = @SecurityRequirement(name = "bearerAuth"),
            deprecated = true
    )
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/system/modules/{moduleKey}/permissions")
    public ModuleDtos.PermissionResponse createPermissionLegacy(@PathVariable String moduleKey,
                                                                @RequestBody ModuleDtos.CreatePermissionRequest req) {
        return createPermission(moduleKey, req);
    }

    @Operation(
            summary = "List permissions for module",
            description = "Returns active permissions that belong to the specified module.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "System admin rights required"),
            @ApiResponse(responseCode = "404", description = "Module not found")
    })
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @GetMapping("/modules/{moduleKey}/permissions")
    public List<ModuleDtos.PermissionResponse> listPermissions(
            @Parameter(in = ParameterIn.PATH, description = "Module key as declared in the catalog", example = "user", required = true)
            @PathVariable String moduleKey) {
        return catalogService.listPermissions(moduleKey).stream()
                .map(p -> new ModuleDtos.PermissionResponse(p.getId(), moduleKey, p.getCode(), p.getDescription(), p.isActive()))
                .toList();
    }

    @Operation(
            summary = "Legacy list permissions",
            description = "Deprecated permission listing endpoint.",
            security = @SecurityRequirement(name = "bearerAuth"),
            deprecated = true
    )
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @GetMapping("/system/modules/{moduleKey}/permissions")
    public List<ModuleDtos.PermissionResponse> listPermissionsLegacy(@PathVariable String moduleKey) {
        return listPermissions(moduleKey);
    }

    @Operation(
            summary = "Enable/disable tenant-specific module",
            description = "Toggles a module for the tenant identified by X-Tenant-Id header.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant module updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "System admin rights required"),
            @ApiResponse(responseCode = "404", description = "Module not found for tenant")
    })
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PutMapping("/tenant/modules/{moduleKey}")
    public void setTenantModuleEnabled(
            @Parameter(in = ParameterIn.HEADER, description = "Tenant identifier activating the module", required = true, name = TENANT_ID, example = "COMPANY-1000")
            @RequestHeader(TENANT_ID) String tenantId,
            @Parameter(in = ParameterIn.PATH, description = "Module key as declared in the catalog", example = "user", required = true)
            @PathVariable String moduleKey,
            @RequestBody ModuleDtos.SetTenantModuleEnabledRequest req) {
        catalogService.setTenantModuleEnabled(tenantId, moduleKey, req.enabled());
    }

    @Operation(
            summary = "Legacy toggle tenant module",
            description = "Deprecated endpoint for toggling tenant/modules.",
            security = @SecurityRequirement(name = "bearerAuth"),
            deprecated = true
    )
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PutMapping("/system/tenants/{tenantId}/modules/{moduleKey}")
    public void setTenantModuleEnabledLegacy(@PathVariable String tenantId,
                                             @PathVariable String moduleKey,
                                             @RequestBody ModuleDtos.SetTenantModuleEnabledRequest req) {
        catalogService.setTenantModuleEnabled(tenantId, moduleKey, req.enabled());
    }

    @Operation(
            summary = "Register a system admin",
            description = "Adds a subject as a system-level administrator.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "System admin added"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "System admin rights required")
    })
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/system/admins")
    public void addSystemAdmin(@RequestBody ModuleDtos.AdminRequest req) {
        adminService.addSystemAdmin(req.subjectId(), SubjectType.valueOf(req.subjectType()));
    }

    @Operation(
            summary = "Legacy register system admin",
            description = "Deprecated endpoint for creating a system admin.",
            security = @SecurityRequirement(name = "bearerAuth"),
            deprecated = true
    )
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PutMapping("/system/admins/system")
    public void addSystemAdminLegacy(@RequestBody ModuleDtos.AdminRequest req) {
        addSystemAdmin(req);
    }
}
