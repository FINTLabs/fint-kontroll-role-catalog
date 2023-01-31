package no.fintlabs.role;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.dto.RoleDTO;
import no.fintlabs.dto.RoleDTOService;
import no.fintlabs.model.Member;
import no.fintlabs.model.Role;
import no.fintlabs.repository.MemberRepository;
import no.fintlabs.member.MemberService;
import no.fintlabs.repository.RoleRepository;
//import no.vigoiks.resourceserver.security.FintJwtEndRolePrincipal;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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
    @Autowired
    private RoleDTOService roleDTOService;

    public Role save(Role role) {
        Set<Member> members = role.getMembers();
        members.forEach(member -> memberService.save(member)
                       );
        log.info("Trying to save role {}", role.getResourceId());
        return roleRepository.save(role);
    }

    public Flux<Role> getAllRoles() {
        List<Role> allRoles = roleRepository.findAll().stream().collect(Collectors.toList());
        return Flux.fromIterable(allRoles);
    }
    public Mono<Role> createNewRole(Role role) {
        Role newRole = roleRepository.save(role);
        return Mono.just(newRole);
    }
    public Mono<Role> findRoleById(Long id) {
        Role role = roleRepository.findById(id).orElse(new Role());
        return Mono.just(role);
    }
    public Mono<Role> findRoleByRoleId(String roleId) {
        Role role = roleRepository.findByRoleId(roleId).orElse(new Role());
        return Mono.just(role);
    }

    public Mono<Role> findRoleByResourceId(String id) {
        Role role = roleRepository.findByResourceId(id).orElse(new Role());
        return Mono.just(role);
    }
    public Flux<RoleDTO> findRolesByMemberId (Long id) {
        List<Role> roles = roleRepository.findRolesByMembersId(id)
                .orElse(new ArrayList<>());

        List< RoleDTO > dtoRoles = new ArrayList<>();
        if (!roles.isEmpty()) {
            dtoRoles = roles
                    .stream()
                    .map(role -> roleDTOService.convertToRoleDTO(role))
                    .toList();
        }

        return Flux.fromIterable(dtoRoles);
    }
}
