package no.fintlabs.roleCatalogRole;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.Member;
import no.fintlabs.membership.Membership;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleService;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembership;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipEntityProducerService;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class RoleCatalogPublishingComponent {

    private final RoleService roleService;
    private final RoleCatalogRoleService roleCatalogRoleService;
    private final RoleCatalogMembershipService roleCatalogMembershipService;
    private final RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService;
    private final RoleCatalogMembershipEntityProducerService roleCatalogMembershipEntityProducerService;

    public RoleCatalogPublishingComponent(
            RoleService roleService,
            RoleCatalogRoleService roleCatalogRoleService,
            RoleCatalogMembershipService roleCatalogMembershipService,
            RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService,
            RoleCatalogMembershipEntityProducerService roleCatalogMembershipEntityProducerService
    ) {
        this.roleService = roleService;
        this.roleCatalogRoleService = roleCatalogRoleService;
        this.roleCatalogMembershipService = roleCatalogMembershipService;
        this.roleCatalogRoleEntityProducerService = roleCatalogRoleEntityProducerService;
        this.roleCatalogMembershipEntityProducerService = roleCatalogMembershipEntityProducerService;
    }

    @Scheduled(
            initialDelayString = "${fint.kontroll.role-catalog.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role-catalog.publishing.fixed-delay}"
    )
//    public void publishRolesAndMemberships() {
//        List<Role> allRoles = roleService.getAllRoles();
//        AtomicReference<Integer> totalNoOfMembers = new AtomicReference<>(0);
//        log.info("Publishing {} roles and memberships started", allRoles.size());
//        allRoles.forEach(role -> {
//                    roleCatalogRoleService.process(roleCatalogRoleService.create(role));
//                    List<Membership> members = role.getMemberships().stream().toList();
//                    log.info("Publishing {} memberships for role {}, roleid: {}, resourceid: {}", members.size(), role.getRoleName(), role.getRoleId(), role.getResourceId());
//                    totalNoOfMembers.updateAndGet(v -> v + members.size());
//                    members.forEach(member -> roleCatalogMembershipService
//                            .process(roleCatalogMembershipService.create(role, member)));
//                });
//        log.info("Publishing {} roles and {} memberships finished", allRoles.size(), totalNoOfMembers);
//    }
    public void publishRoles() {
        List<RoleCatalogRole> allCatalogRoles = roleService.getAllRoles()
                .stream()
                .map(roleCatalogRoleService::create)
                .toList();

        List<RoleCatalogRole> publishedRoles = roleCatalogRoleEntityProducerService.publishChangedCatalogRoles(allCatalogRoles);

        log.info("Published {} of {} role catalog roles", publishedRoles.size(), allCatalogRoles.size());
        log.info("Ids of published role catalog roles: {}",
                publishedRoles.stream()
                        .map(RoleCatalogRole::getRoleId)
                        .toList()
        );
    }

    //TODO: Maybe publishMemberships should be in a separate class like MembershipPublishingComponent with a separate schedule
    @Scheduled(
            initialDelayString = "${fint.kontroll.role-catalog.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role-catalog.publishing.fixed-delay}"
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
}
