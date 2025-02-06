package no.fintlabs.roleCatalogRole;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.role.Role;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RoleCatalogRoleService {
    private final RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService;

    public RoleCatalogRoleService(RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService) {
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
                .noOfMembers(getNoOfActiveMemberships(role))
                .roleType(role.getRoleType())
                .roleName(role.getRoleName())
                .organisationUnitId(role.getOrganisationUnitId())
                .organisationUnitName(role.getOrganisationUnitName())
                .roleStatus(role.getRoleStatus())
                .roleStatusChanged(role.getRoleStatusChanged())
                .build();
    }

    private Integer getNoOfActiveMemberships(Role role) {
        Integer noOfActiveMemberships = role.getMemberships()
                .stream()
                .filter(membership -> membership.getMembershipStatus().equals("ACTIVE"))
                .toList()
                .size();
        log.info("Role {} ({})  has {} active memberships", role.getId(), role.getRoleName(), noOfActiveMemberships);
        return noOfActiveMemberships;
    }
}
