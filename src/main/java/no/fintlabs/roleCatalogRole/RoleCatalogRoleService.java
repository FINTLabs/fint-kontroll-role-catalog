package no.fintlabs.roleCatalogRole;

import no.fintlabs.roleCatalogRole.RoleCatalogRole;
import no.fintlabs.role.Role;
import org.springframework.stereotype.Service;

@Service
public class RoleCatalogRoleService {
    private final RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService;

    public RoleCatalogRoleService(RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService) {
        this.roleCatalogRoleEntityProducerService = roleCatalogRoleEntityProducerService;
    }
    public void process(RoleCatalogRole roleCatalogRole) {
        roleCatalogRoleEntityProducerService.publish(roleCatalogRole);
    }
    public RoleCatalogRole create(Role role) {
        return RoleCatalogRole.builder()
                .id(role.getId())
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .noOfMembers(role.getNoOfMembers())
                .roleType(role.getRoleType())
                .roleName(role.getRoleName())
                .organisationUnitId(role.getOrganisationUnitId())
                .organisationUnitName(role.getOrganisationUnitName())
                .build();
    }
}
