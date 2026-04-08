package no.fintlabs.roleCatalogMembership;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import no.fintlabs.membership.Membership;
import no.fintlabs.role.Role;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleCatalogMembershipService {

    public RoleCatalogMembership create(Role role, Membership membership) {
        return RoleCatalogMembership.builder()
                .id(getRoleCatalogMembershipId(role, membership))
                .roleId(role.getId())
                .memberId(membership.getMember().getId())
                .identityProviderUserObjectId(membership.getMember().getIdentityProviderUserObjectId())
                .memberStatus(membership.getMembershipStatus() == null ? "ACTIVE" : membership.getMembershipStatus())
                .memberStatusChanged(membership.getMembershipStatusChanged())
                .build();
    }

    public static String getRoleCatalogMembershipId(Role role, Membership membership) {
        return role.getId().toString() + "_" + membership.getMember().getId();
    }
}
