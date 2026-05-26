package no.fintlabs.roleCatalogMembership;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.membership.Membership;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleCatalogMembershipPublishingComponent {

    private final RoleRepository roleRepository;
    private final RoleCatalogMembershipService roleCatalogMembershipService;
    private final RoleCatalogMembershipEntityProducerService roleCatalogMembershipEntityProducerService;

    @Scheduled(
            cron = "${fint.kontroll.role-catalog.publishing.cron-membership}"
    )
    public void publishMemberships() {
        List<RoleCatalogMembership> allCatalogMemberships = roleRepository.findAll()
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

    public void publishMembershipsForRole(Role roleToPublish) {
        List<RoleCatalogMembership> roleCatalogMemberships = roleToPublish.getMemberships()
                .stream()
                .map(member -> roleCatalogMembershipService.create(roleToPublish, member))
                .toList();

        roleCatalogMembershipEntityProducerService
                .publishChangedCatalogMemberships(roleCatalogMemberships);

        log.info("Published {} rolecatalog memberships for role with id: {}",
               roleCatalogMemberships.size(), roleToPublish.getId());
    }
}
