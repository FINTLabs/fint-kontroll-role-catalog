package no.fintlabs.role;

import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.stream.Collectors;

public class RoleMapper {
    public static RoleDto toRoleDto(Role role) {
        return new RoleDto (
                role.getId(),
                role.getRoleName(),
                role.getRoleType(),
                role.getRoleSubType(),
                role.isAggregatedRole(),
                role.getOrganisationUnitId(),
                role.getOrganisationUnitName()
        );
    }
    public static Map<String, Object> toRoleDtoPage(Page<Role> rolePage) {
        return Map.of(
                "roles",
                    rolePage.getContent()
                        .stream()
                        .map(RoleMapper::toRoleDto)
                        .collect(Collectors.toList()),
                "currentPage", rolePage.getNumber(),
                "totalPages", rolePage.getTotalPages(),
                "size", rolePage.getSize(),
                "totalItems", rolePage.getTotalElements()
        );
    }
}
