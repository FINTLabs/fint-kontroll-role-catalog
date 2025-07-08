package no.fintlabs.roleCatalogMembership;

import no.fintlabs.membership.Membership;
import no.fintlabs.role.Role;
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
    public RoleCatalogMembership create(Role role, Membership membership) {
        return RoleCatalogMembership.builder()
                .id(role.getId().toString() + "_" + membership.getMember().getId())
                .roleId(role.getId())
                .memberId(membership.getMember().getId())
                .identityProviderUserObjectId(membership.getMember().getIdentityProviderUserObjectId())
                .memberStatus(membership.getMembershipStatus() == null ? "ACTIVE" : membership.getMembershipStatus())
                .memberStatusChanged(membership.getMembershipStatusChanged())
                .build();
    }
}
