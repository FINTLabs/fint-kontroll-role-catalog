package no.fintlabs.roleCatalogRole;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.Member;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleService;
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
    public RoleCatalogPublishingComponent(
            RoleService roleService,
            RoleCatalogRoleService roleCatalogRoleService,
            RoleCatalogMembershipService roleCatalogMembershipService
    ) {
        this.roleService = roleService;
        this.roleCatalogRoleService = roleCatalogRoleService;
        this.roleCatalogMembershipService = roleCatalogMembershipService;
    }
    @Scheduled(
            initialDelayString = "${fint.kontroll.role-catalog.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role-catalog.publishing.fixed-delay}"
    )
    public void publishRolesAndMemberships() {
        List<Role> allRoles = roleService.getAllRoles();
        AtomicReference<Integer> totalNoOfMembers = new AtomicReference<>(0);
        log.info("Publishing {} roles and memberships started", allRoles.size());
        allRoles.forEach(role -> {
                    roleCatalogRoleService.process(roleCatalogRoleService.create(role));
                    List<Member> members = role.getMembers().stream().toList();
                    log.info("Publishing {} memberships for role {}",members.size(), role.getRoleName());
                    totalNoOfMembers.updateAndGet(v -> v + members.size());
                    members.forEach(member -> roleCatalogMembershipService
                            .process(roleCatalogMembershipService.create(role, member)));
                });
        log.info("Publishing {} roles and {} memberships finished", allRoles.size(), totalNoOfMembers);
    }
}
