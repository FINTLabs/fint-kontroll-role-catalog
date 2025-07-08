package no.fintlabs.roleCatalogRole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.role.RoleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleCatalogPublishingComponent {

    private final RoleService roleService;
    private final RoleCatalogRoleService roleCatalogRoleService;
    private final RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService;

    @Scheduled(
            initialDelayString = "${fint.kontroll.role-catalog.publishing.initial-delay}",
            fixedDelayString = "${fint.kontroll.role-catalog.publishing.fixed-delay}"
    )
    public void publishRoles() {
        List<RoleCatalogRole> allCatalogRoles = roleService.getAllRoles()
                .stream()
                .map(roleCatalogRoleService::mapToRoleCatalogRole)
                .toList();

        List<RoleCatalogRole> publishedRoles = roleCatalogRoleEntityProducerService.publishChangedCatalogRoles(allCatalogRoles);

        log.info("Published {} of {} role catalog roles", publishedRoles.size(), allCatalogRoles.size());
        publishedRoles.forEach(roleCatalogRole ->
                        log.info("Published role catalog role: id {} - name {} - no of members {}",
                                roleCatalogRole.getId(),
                                roleCatalogRole.getRoleName(),
                                roleCatalogRole.getNoOfMembers()
                        ));
    }
}
