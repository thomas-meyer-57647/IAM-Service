package de.innologic.iamservice.api.dto;

public class ModuleDtos {

    public record CreateModuleRequest(String moduleKey, String name, String description) {}
    public record ModuleResponse(Long id, String moduleKey, String name, String description, boolean active) {}

    public record CreatePermissionRequest(String code, String description) {}
    public record PermissionResponse(Long id, String moduleKey, String code, String description, boolean active) {}

    public record SetTenantModuleEnabledRequest(boolean enabled) {}

    public record CreateRoleRequest(String name, String description) {}
    public record RoleResponse(Long id, String tenantId, String name, String description, boolean active) {}

    public record SetRolePermissionsRequest(java.util.List<String> permissionCodes) {}

    // TenantId kommt via Header X-Tenant-Id
    public record AssignRoleRequest(String subjectId, String subjectType, Long roleId) {}

    public record AccessResponse(boolean enabled, java.util.List<String> permissions, long permVersion) {}

    public record AdminRequest(String subjectId, String subjectType) {}
}
