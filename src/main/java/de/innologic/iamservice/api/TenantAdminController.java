package de.innologic.iamservice.api;

import de.innologic.iamservice.api.dto.ModuleDtos;
import de.innologic.iamservice.admin.service.AdminService;
import de.innologic.iamservice.domain.SubjectType;
import de.innologic.iamservice.role.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;

import static de.innologic.iamservice.api.TenantHeaders.TENANT_ID;

@RestController
@RequestMapping("/v1/tenant")
@Tag(name = "Tenant Administration", description = "Endpoints for tenant-specific roles, assignments and admins")
public class TenantAdminController {

    private final RoleService roleService;
    private final AdminService adminService;

    public TenantAdminController(RoleService roleService, AdminService adminService) {
        this.roleService = roleService;
        this.adminService = adminService;
    }

    @Operation(
            summary = "Create a tenant role",
            description = "Tenant admins can define roles that bundle permissions.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Tenant admin rights required"),
            @ApiResponse(responseCode = "409", description = "Role name already exists")
    })
    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @PostMapping("/roles")
    public ModuleDtos.RoleResponse createRole(
            @Parameter(in = ParameterIn.HEADER, name = TENANT_ID, description = "Tenant identifier that scopes the role", required = true, example = "COMPANY-100")
            @RequestHeader(TENANT_ID) String tenantId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role definition containing display name and description",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ModuleDtos.CreateRoleRequest.class))
            )
            @RequestBody ModuleDtos.CreateRoleRequest req) {
        var r = roleService.createRole(tenantId, req.name(), req.description());
        return new ModuleDtos.RoleResponse(r.getId(), r.getTenantId(), r.getName(), r.getDescription(), r.isActive());
    }

    @Operation(
            summary = "List roles",
            description = "Lists the roles defined for the tenant.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Tenant admin rights required")
    })
    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @GetMapping("/roles")
    public List<ModuleDtos.RoleResponse> listRoles(
            @Parameter(in = ParameterIn.HEADER, name = TENANT_ID, description = "Tenant identifier whose roles are requested", required = true, example = "COMPANY-100")
            @RequestHeader(TENANT_ID) String tenantId) {
        return roleService.listRoles(tenantId).stream()
                .map(r -> new ModuleDtos.RoleResponse(r.getId(), r.getTenantId(), r.getName(), r.getDescription(), r.isActive()))
                .toList();
    }

    @Operation(
            summary = "Set permissions for a role",
            description = "Replaces the permission set of the specified role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Tenant admin rights required"),
            @ApiResponse(responseCode = "404", description = "Role or permissions not found")
    })
    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @PutMapping("/roles/{roleId}/permissions")
    public void setRolePermissions(
            @Parameter(in = ParameterIn.HEADER, name = TENANT_ID, description = "Tenant identifier owning the role", required = true, example = "COMPANY-100")
            @RequestHeader(TENANT_ID) String tenantId,
            @Parameter(in = ParameterIn.PATH, description = "Identifier of the role to update", required = true, example = "15")
            @PathVariable Long roleId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Complete permission code list that replaces the current set",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ModuleDtos.SetRolePermissionsRequest.class))
            )
            @RequestBody ModuleDtos.SetRolePermissionsRequest req) {
        roleService.setRolePermissions(tenantId, roleId, req.permissionCodes());
    }

    @Operation(
            summary = "Assign a role to a subject",
            description = "Creates or restores the assignment of a role to the subject.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assignment created"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Tenant admin rights required"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @PostMapping("/assignments")
    public void assignRole(
            @Parameter(in = ParameterIn.HEADER, name = TENANT_ID, description = "Tenant identifier whose subject receives the role", required = true, example = "COMPANY-100")
            @RequestHeader(TENANT_ID) String tenantId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Subject and role identifiers that define the assignment",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ModuleDtos.AssignRoleRequest.class))
            )
            @RequestBody ModuleDtos.AssignRoleRequest req) {
        roleService.assignRole(tenantId, req.subjectId(), SubjectType.valueOf(req.subjectType()), req.roleId());
    }

    @Operation(
            summary = "Add tenant admin",
            description = "Adds or restores a tenant admin subject.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant admin added"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Tenant admin rights required")
    })
    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @PutMapping("/admins")
    public void addTenantAdmin(
            @Parameter(in = ParameterIn.HEADER, name = TENANT_ID, description = "Tenant identifier for the admin license", required = true, example = "COMPANY-100")
            @RequestHeader(TENANT_ID) String tenantId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Subject data used to create the tenant admin",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ModuleDtos.AdminRequest.class))
            )
            @RequestBody ModuleDtos.AdminRequest req) {
        adminService.addTenantAdmin(tenantId, req.subjectId(), SubjectType.valueOf(req.subjectType()));
    }

    @Operation(
            summary = "Remove tenant admin",
            description = "Deletes a tenant admin unless it is the last admin in the tenant.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant admin removed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Tenant admin rights required"),
            @ApiResponse(responseCode = "409", description = "Cannot remove last tenant admin")
    })
    @PreAuthorize("@iamAuthz.isTenantAdmin(authentication, #tenantId)")
    @DeleteMapping("/admins")
    public void removeTenantAdmin(
            @Parameter(in = ParameterIn.HEADER, name = TENANT_ID, description = "Tenant identifier for the admin removal", required = true, example = "COMPANY-100")
            @RequestHeader(TENANT_ID) String tenantId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Subject identifying the admin to remove",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ModuleDtos.AdminRequest.class))
            )
            @RequestBody ModuleDtos.AdminRequest req) {
        adminService.removeTenantAdmin(tenantId, req.subjectId(), SubjectType.valueOf(req.subjectType()));
    }
}

