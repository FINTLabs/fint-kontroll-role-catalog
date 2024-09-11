package no.fintlabs.roleCatalogMembership;

import no.fintlabs.member.Member;
import no.fintlabs.membership.Membership;
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
    public RoleCatalogMembership create(Role role, Membership member) {
        return RoleCatalogMembership.builder()
                .id(role.getId().toString() + "_" + member.getMember().getId())
                .roleId(role.getId())
                .memberId(member.getMember().getId())
                .identityProviderUserObjectId(member.getMember().getIdentityProviderUserObjectId())
                .build();
    }
}
