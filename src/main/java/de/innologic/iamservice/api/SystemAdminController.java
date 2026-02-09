package de.innologic.iamservice.api;

import de.innologic.iamservice.api.dto.ModuleDtos;
import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.catalog.service.CatalogService;
import de.innologic.iamservice.domain.SubjectType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/system")
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

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @GetMapping("/modules")
    public List<ModuleDtos.ModuleResponse> listModules() {
        return catalogService.listModules().stream()
                .map(m -> new ModuleDtos.ModuleResponse(m.getId(), m.getModuleKey(), m.getName(), m.getDescription(), m.isActive()))
                .toList();
    }

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PostMapping("/modules/{moduleKey}/permissions")
    public ModuleDtos.PermissionResponse createPermission(@PathVariable String moduleKey,
                                                          @RequestBody ModuleDtos.CreatePermissionRequest req) {
        var p = catalogService.createPermission(moduleKey, req.code(), req.description());
        return new ModuleDtos.PermissionResponse(p.getId(), moduleKey, p.getCode(), p.getDescription(), p.isActive());
    }

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @GetMapping("/modules/{moduleKey}/permissions")
    public List<ModuleDtos.PermissionResponse> listPermissions(@PathVariable String moduleKey) {
        return catalogService.listPermissions(moduleKey).stream()
                .map(p -> new ModuleDtos.PermissionResponse(p.getId(), moduleKey, p.getCode(), p.getDescription(), p.isActive()))
                .toList();
    }

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PutMapping("/tenants/{tenantId}/modules/{moduleKey}")
    public void setTenantModuleEnabled(@PathVariable String tenantId,
                                       @PathVariable String moduleKey,
                                       @RequestBody ModuleDtos.SetTenantModuleEnabledRequest req) {
        catalogService.setTenantModuleEnabled(tenantId, moduleKey, req.enabled());
    }

    @PreAuthorize("@iamAuthz.isSystemAdmin(authentication)")
    @PutMapping("/admins/system")
    public void addSystemAdmin(@RequestBody ModuleDtos.AdminRequest req) {
        adminService.addSystemAdmin(req.subjectId(), SubjectType.valueOf(req.subjectType()));
    }
}
