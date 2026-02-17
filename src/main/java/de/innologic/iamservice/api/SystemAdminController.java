package de.innologic.iamservice.api;

import de.innologic.iamservice.api.dto.ModuleDtos;
import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.catalog.service.CatalogService;
import de.innologic.iamservice.domain.SubjectType;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.innologic.iamservice.api.TenantHeaders.TENANT_ID;

@RestController
@RequestMapping("/v1")
public class SystemAdminController {

    private final CatalogService catalogService;
    private final AdminService adminService;

    public SystemAdminController(CatalogService catalogService, AdminService adminService) {
        this.catalogService = catalogService;
        this.adminService = adminService;
    }

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/modules")
    public ModuleDtos.ModuleResponse createModule(@RequestBody ModuleDtos.CreateModuleRequest req) {
        var m = catalogService.createModule(req.moduleKey(), req.name(), req.description());
        return new ModuleDtos.ModuleResponse(m.getId(), m.getModuleKey(), m.getName(), m.getDescription(), m.isActive());
    }

    @Operation(deprecated = true)
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/system/modules")
    public ModuleDtos.ModuleResponse createModuleLegacy(@RequestBody ModuleDtos.CreateModuleRequest req) {
        return createModule(req);
    }

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @GetMapping("/modules")
    public List<ModuleDtos.ModuleResponse> listModules() {
        return catalogService.listModules().stream()
                .map(m -> new ModuleDtos.ModuleResponse(m.getId(), m.getModuleKey(), m.getName(), m.getDescription(), m.isActive()))
                .toList();
    }

    @Operation(deprecated = true)
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @GetMapping("/system/modules")
    public List<ModuleDtos.ModuleResponse> listModulesLegacy() {
        return listModules();
    }

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/modules/{moduleKey}/permissions")
    public ModuleDtos.PermissionResponse createPermission(@PathVariable String moduleKey,
                                                          @RequestBody ModuleDtos.CreatePermissionRequest req) {
        var p = catalogService.createPermission(moduleKey, req.code(), req.description());
        return new ModuleDtos.PermissionResponse(p.getId(), moduleKey, p.getCode(), p.getDescription(), p.isActive());
    }

    @Operation(deprecated = true)
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/system/modules/{moduleKey}/permissions")
    public ModuleDtos.PermissionResponse createPermissionLegacy(@PathVariable String moduleKey,
                                                                @RequestBody ModuleDtos.CreatePermissionRequest req) {
        return createPermission(moduleKey, req);
    }

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @GetMapping("/modules/{moduleKey}/permissions")
    public List<ModuleDtos.PermissionResponse> listPermissions(@PathVariable String moduleKey) {
        return catalogService.listPermissions(moduleKey).stream()
                .map(p -> new ModuleDtos.PermissionResponse(p.getId(), moduleKey, p.getCode(), p.getDescription(), p.isActive()))
                .toList();
    }

    @Operation(deprecated = true)
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @GetMapping("/system/modules/{moduleKey}/permissions")
    public List<ModuleDtos.PermissionResponse> listPermissionsLegacy(@PathVariable String moduleKey) {
        return listPermissions(moduleKey);
    }

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PutMapping("/tenant/modules/{moduleKey}")
    public void setTenantModuleEnabled(@RequestHeader(TENANT_ID) String tenantId,
                                       @PathVariable String moduleKey,
                                       @RequestBody ModuleDtos.SetTenantModuleEnabledRequest req) {
        catalogService.setTenantModuleEnabled(tenantId, moduleKey, req.enabled());
    }

    @Operation(deprecated = true)
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PutMapping("/system/tenants/{tenantId}/modules/{moduleKey}")
    public void setTenantModuleEnabledLegacy(@PathVariable String tenantId,
                                             @PathVariable String moduleKey,
                                             @RequestBody ModuleDtos.SetTenantModuleEnabledRequest req) {
        catalogService.setTenantModuleEnabled(tenantId, moduleKey, req.enabled());
    }

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/system/admins")
    public void addSystemAdmin(@RequestBody ModuleDtos.AdminRequest req) {
        adminService.addSystemAdmin(req.subjectId(), SubjectType.valueOf(req.subjectType()));
    }

    @Operation(deprecated = true)
    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PutMapping("/system/admins/system")
    public void addSystemAdminLegacy(@RequestBody ModuleDtos.AdminRequest req) {
        addSystemAdmin(req);
    }
}
