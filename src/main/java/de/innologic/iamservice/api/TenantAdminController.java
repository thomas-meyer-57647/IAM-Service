package de.innologic.iamservice.api;

import de.innologic.iamservice.api.dto.ModuleDtos;
import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.role.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.innologic.iamservice.api.TenantHeaders.TENANT_ID;

@RestController
@RequestMapping("/v1/tenant")
public class TenantAdminController {

    private final RoleService roleService;
    private final AdminService adminService;

    public TenantAdminController(RoleService roleService, AdminService adminService) {
        this.roleService = roleService;
        this.adminService = adminService;
    }

    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @PostMapping("/roles")
    public ModuleDtos.RoleResponse createRole(@RequestHeader(TENANT_ID) String tenantId,
                                              @RequestBody ModuleDtos.CreateRoleRequest req) {
        var r = roleService.createRole(tenantId, req.name(), req.description());
        return new ModuleDtos.RoleResponse(r.getId(), r.getTenantId(), r.getName(), r.getDescription(), r.isActive());
    }

    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @GetMapping("/roles")
    public List<ModuleDtos.RoleResponse> listRoles(@RequestHeader(TENANT_ID) String tenantId) {
        return roleService.listRoles(tenantId).stream()
                .map(r -> new ModuleDtos.RoleResponse(r.getId(), r.getTenantId(), r.getName(), r.getDescription(), r.isActive()))
                .toList();
    }

    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @PutMapping("/roles/{roleId}/permissions")
    public void setRolePermissions(@RequestHeader(TENANT_ID) String tenantId,
                                   @PathVariable Long roleId,
                                   @RequestBody ModuleDtos.SetRolePermissionsRequest req) {
        roleService.setRolePermissions(tenantId, roleId, req.permissionCodes());
    }

    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @PostMapping("/assignments")
    public void assignRole(@RequestHeader(TENANT_ID) String tenantId,
                           @RequestBody ModuleDtos.AssignRoleRequest req) {
        roleService.assignRole(tenantId, req.subjectId(), SubjectType.valueOf(req.subjectType()), req.roleId());
    }

    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @PutMapping("/admins")
    public void addTenantAdmin(@RequestHeader(TENANT_ID) String tenantId,
                               @RequestBody ModuleDtos.AdminRequest req) {
        adminService.addTenantAdmin(tenantId, req.subjectId(), SubjectType.valueOf(req.subjectType()));
    }

    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @DeleteMapping("/admins")
    public void removeTenantAdmin(@RequestHeader(TENANT_ID) String tenantId,
                                  @RequestBody ModuleDtos.AdminRequest req) {
        adminService.removeTenantAdmin(tenantId, req.subjectId(), SubjectType.valueOf(req.subjectType()));
    }
}

