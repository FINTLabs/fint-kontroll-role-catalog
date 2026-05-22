package no.fintlabs.roleCatalogMembership;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.membership.Membership;
import no.fintlabs.role.RoleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleCatalogMembershipPublishingComponent {

    private final RoleService roleService;
    private final RoleCatalogMembershipService roleCatalogMembershipService;
    private final RoleCatalogMembershipEntityProducerService roleCatalogMembershipEntityProducerService;

    @Scheduled(
            cron = "${fint.kontroll.role-catalog.publishing.cron}"
    )
    public void publishMemberships() {
        List<RoleCatalogMembership> allCatalogMemberships = roleService.getAllRoles()
                .stream()
                .map(role -> role.getMemberships()
                        .stream()
                        .map(member -> roleCatalogMembershipService.create(role, member))
                        .toList())
                .flatMap(List::stream)
                .toList();

        List<RoleCatalogMembership> publishedMemberships = roleCatalogMembershipEntityProducerService.publishChangedCatalogMemberships(allCatalogMemberships);

        log.info("Published {} of {} role catalog memberships", publishedMemberships.size(), allCatalogMemberships.size());
    }

    public void publishMembership(Membership membership) {
        RoleCatalogMembership roleCatalogMembership = roleCatalogMembershipService.create(membership);
        roleCatalogMembershipEntityProducerService.publishChangedCatalogMemberships(roleCatalogMembership);
    }
}
