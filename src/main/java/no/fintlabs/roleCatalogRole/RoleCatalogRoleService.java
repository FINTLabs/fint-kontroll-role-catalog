package no.fintlabs.roleCatalogRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.role.Role;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleCatalogRoleService {

    public RoleCatalogRole mapToRoleCatalogRole(Role role) {
        return RoleCatalogRole.builder()
                .id(role.getId())
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .noOfMembers(role.getNoOfMembers())
                .roleType(role.getRoleType())
                .roleName(role.getRoleName())
                .organisationUnitId(role.getOrganisationUnitId())
                .organisationUnitName(role.getOrganisationUnitName())
                .roleStatus(role.getRoleStatus())
                .roleStatusChanged(role.getRoleStatusChanged())
                .build();
    }
}
