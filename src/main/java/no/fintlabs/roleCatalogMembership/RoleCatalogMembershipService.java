package no.fintlabs.roleCatalogMembership;

import no.fintlabs.member.Member;
import no.fintlabs.role.Role;
import no.fintlabs.roleCatalogRole.RoleCatalogRole;
import no.fintlabs.roleCatalogRole.RoleCatalogRoleEntityProducerService;
import org.springframework.stereotype.Service;

@Service
public class RoleCatalogMembershipService {
    private final RoleCatalogMembershipEntityProducerService roleCatalogMembershipEntityProducerService;

    public RoleCatalogMembershipService(RoleCatalogMembershipEntityProducerService roleCatalogMembershipEntityProducerService) {
        this.roleCatalogMembershipEntityProducerService = roleCatalogMembershipEntityProducerService;
    }
    public void process(RoleCatalogMembership roleCatalogMembership) {
        roleCatalogMembershipEntityProducerService.publish(roleCatalogMembership);
    }
    public RoleCatalogMembership create(Role role, Member member) {
        return RoleCatalogMembership.builder()
                .id(role.getId().toString() + "_" + member.getId().toString())
                .roleId(role.getId())
                .memberId(member.getId())
                .identityProviderUserObjectId(member.getIdentityProviderUserObjectId())
                .build();
    }
}
