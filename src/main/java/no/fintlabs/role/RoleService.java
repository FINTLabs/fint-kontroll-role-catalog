package no.fintlabs.role;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.Member;
import no.fintlabs.model.Role;
import no.fintlabs.repository.MemberRepository;
import no.fintlabs.member.MemberService;
import no.fintlabs.repository.RoleRepository;
//import no.vigoiks.resourceserver.security.FintJwtEndRolePrincipal;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MemberService memberService;

    public Role save(Role role) {
        Set<Member> members = role.getMembers();
        members.forEach(member -> memberService.save(member)
                       );
        log.info("Trying to save role {}", role.getResourceId());
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
