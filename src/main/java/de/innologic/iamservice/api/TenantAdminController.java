package de.innologic.iamservice.api;

import de.innologic.iamservice.api.dto.ModuleDtos;
import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.role.service.RoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/tenant")
public class TenantAdminController {

    private final RoleService roleService;
    private final AdminService adminService;

    public TenantAdminController(RoleService roleService, AdminService adminService) {
        this.roleService = roleService;
        this.adminService = adminService;
    }

    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    @PostMapping("/{tenantId}/roles")
    public ModuleDtos.RoleResponse createRole(@PathVariable String tenantId,
                                              @RequestBody ModuleDtos.CreateRoleRequest req) {
        var r = roleService.createRole(tenantId, req.name(), req.description());
        return new ModuleDtos.RoleResponse(r.getId(), r.getTenantId(), r.getName(), r.getDescription(), r.isActive());
    }

    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    @GetMapping("/{tenantId}/roles")
    public List<ModuleDtos.RoleResponse> listRoles(@PathVariable String tenantId) {
        return roleService.listRoles(tenantId).stream()
                .map(r -> new ModuleDtos.RoleResponse(r.getId(), r.getTenantId(), r.getName(), r.getDescription(), r.isActive()))
                .toList();
    }

    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    @PutMapping("/{tenantId}/roles/{roleId}/permissions")
    public void setRolePermissions(@PathVariable String tenantId,
                                   @PathVariable Long roleId,
                                   @RequestBody ModuleDtos.SetRolePermissionsRequest req) {
        roleService.setRolePermissions(tenantId, roleId, req.permissionCodes());
    }

    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    @PostMapping("/{tenantId}/assignments")
    public void assignRole(@PathVariable String tenantId,
                           @RequestBody ModuleDtos.AssignRoleRequest req) {
        roleService.assignRole(tenantId, req.subjectId(), SubjectType.valueOf(req.subjectType()), req.roleId());
    }

    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    @PutMapping("/{tenantId}/admins")
    public void addTenantAdmin(@PathVariable String tenantId,
                               @RequestBody ModuleDtos.AdminRequest req) {
        adminService.addTenantAdmin(tenantId, req.subjectId(), SubjectType.valueOf(req.subjectType()));
    }

    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    @DeleteMapping("/{tenantId}/admins")
    public void removeTenantAdmin(@PathVariable String tenantId,
                                  @RequestBody ModuleDtos.AdminRequest req) {
        adminService.removeTenantAdmin(tenantId, req.subjectId(), SubjectType.valueOf(req.subjectType()));
    }
}
