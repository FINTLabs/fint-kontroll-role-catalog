package no.fintlabs.role;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.Role;
import no.fintlabs.repository.RoleRepository;
//import no.vigoiks.resourceserver.security.FintJwtEndRolePrincipal;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public Flux<Role> getAllRoles() {
        List<Role> allRoles  = roleRepository.findAll().stream().collect(Collectors.toList());
        return Flux.fromIterable(allRoles);
    }
    public Mono<Role> findRoleById(Long id) {
        Role role = roleRepository.findById(id).orElse(new Role());
        return Mono.just(role);
    }

    public Mono<Role> findRoleByResourceId(String id) {
        Role role = roleRepository.findByResourceId(id).orElse(new Role());
        return Mono.just(role);
    }
}
