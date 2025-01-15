package no.fintlabs.role;

public record RoleDto(
        Long id,
        String roleName,
        String roleType,
        String roleSubType,
        boolean aggregatedRole,
        String organisationUnitId,
        String organisationUnitName
) {}
