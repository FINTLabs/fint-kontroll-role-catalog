package no.fintlabs.dto;

import no.fintlabs.model.Role;
import org.springframework.stereotype.Service;

@Service
public class RoleDTOService {
    public RoleDTO convertToRoleDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .roleType(role.getRoleType())
                .aggregatedRole(role.isAggregatedRole())
                .resourceId(role.getResourceId())
                .build();
    }
}
