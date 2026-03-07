package de.innologic.iamservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ModuleDtos {

    public record CreateModuleRequest(
            @Schema(description = "System-wide unique module key", example = "user") String moduleKey,
            @Schema(description = "Display name of the module", example = "User Management") String name,
            @Schema(description = "Detailed explanation of the module", example = "Manage user accounts and permissions") String description
    ) {}

    public record ModuleResponse(
            @Schema(description = "Persisted module identifier", example = "15") Long id,
            @Schema(description = "Module key", example = "user") String moduleKey,
            @Schema(description = "Human-friendly name", example = "User Management") String name,
            @Schema(description = "Module description", example = "Users and roles catalog") String description,
            @Schema(description = "Indicates if the module remains active", example = "true") boolean active
    ) {}

    public record CreatePermissionRequest(
            @Schema(description = "Permission code must follow moduleKey.action", example = "user.read") String code,
            @Schema(description = "Permission description", example = "Read user data") String description
    ) {}

    public record PermissionResponse(
            @Schema(description = "Permission identifier", example = "101") Long id,
            @Schema(description = "Parent module key", example = "user") String moduleKey,
            @Schema(description = "Permission code", example = "user.read") String code,
            @Schema(description = "Permission description", example = "Read access to users") String description,
            @Schema(description = "Flag marking the permission as active", example = "true") boolean active
    ) {}

    public record SetTenantModuleEnabledRequest(
            @Schema(description = "Enablement flag for the module in the tenant scope", example = "true") boolean enabled
    ) {}

    public record CreateRoleRequest(
            @Schema(description = "Role name unique within tenant", example = "Tenant Administrator") String name,
            @Schema(description = "Role description", example = "Manages tenant-specific IAM functions") String description
    ) {}

    public record RoleResponse(
            @Schema(description = "Role identifier", example = "12") Long id,
            @Schema(description = "Tenant identifier", example = "COMPANY-100") String tenantId,
            @Schema(description = "Role name", example = "Tenant Administrator") String name,
            @Schema(description = "Role description", example = "Tenant administration tasks") String description,
            @Schema(description = "Active indicator", example = "true") boolean active
    ) {}

    public record SetRolePermissionsRequest(
            @Schema(description = "Complete list of permission codes for the role", example = "[\"user.read\",\"user.write\"]") java.util.List<String> permissionCodes
    ) {}

    // TenantId kommt via Header X-Tenant-Id
    public record AssignRoleRequest(
            @Schema(description = "Subject identifier to assign the role", example = "USR-10001") String subjectId,
            @Schema(description = "Subject type (USER or SERVICE)", example = "USER") String subjectType,
            @Schema(description = "Role identifier to bind", example = "15") Long roleId
    ) {}

    public record AccessResponse(
            @Schema(description = "Indicates whether the module is enabled for the tenant", example = "true") boolean enabled,
            @Schema(description = "Granted permission codes", example = "[\"user.read\",\"user.write\"]") java.util.List<String> permissions,
            @Schema(description = "Permission version number for the tenant/subject", example = "7") long permVersion
    ) {}

    public record AdminRequest(
            @Schema(description = "Subject identifier being promoted", example = "USR-10001") String subjectId,
            @Schema(description = "Subject type for the admin (USER or SERVICE)", example = "USER") String subjectType
    ) {}
}
