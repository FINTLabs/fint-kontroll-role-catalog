package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RolePersistingComponent {
    private final RoleService roleService;
    public RolePersistingComponent(RoleService roleService) {
        this.roleService = roleService;
    }
    @Scheduled(
            initialDelayString = "${fint.kontroll.role-catalog.persisting.initial-delay}",
            fixedDelayString = "${fint.kontroll.role-catalog.persisting.fixed-delay}"
    )
    public void persistRoles() {
        log.info("Reading all roles from roleCache");
        List<Role> allRoles = roleService.getAllRolesFromCache();
        log.info("Persisting {} roles from role cache started", allRoles.size());
        allRoles.forEach(role-> roleService.save(role));
        log.info("Persisting {} roles from role cache finished", allRoles.size());
    }
}
