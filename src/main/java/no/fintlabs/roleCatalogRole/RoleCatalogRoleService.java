package no.fintlabs.roleCatalogRole;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RoleCatalogRoleService {
    private final RoleService roleService;
    private final RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService;

    public RoleCatalogRoleService(RoleService roleService, RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService) {
        this.roleService = roleService;
        this.roleCatalogRoleEntityProducerService = roleCatalogRoleEntityProducerService;
    }
//    public void process(RoleCatalogRole roleCatalogRole) {
//        roleCatalogRoleEntityProducerService.publish(roleCatalogRole);
//    }
    public RoleCatalogRole create(Role role) {
        return RoleCatalogRole.builder()
                .id(role.getId())
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .noOfMembers(role.getNoOfMemberships())
                .roleType(role.getRoleType())
                .roleName(role.getRoleName())
                .organisationUnitId(role.getOrganisationUnitId())
                .organisationUnitName(role.getOrganisationUnitName())
                .roleStatus(role.getRoleStatus())
                .roleStatusChanged(role.getRoleStatusChanged())
                .build();
    }
}
