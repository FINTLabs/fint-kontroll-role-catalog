package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
//import no.vigoiks.resourceserver.security.FintJwtEndRolePrincipal;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
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

    public Mono<DetailedRole> GetDetailedRoleById(Long id) {
        return Mono.just(roleRepository.findById(id)
                .map(Role::toDetailedRole)
                .orElse(new DetailedRole())
        );
    }

    public List<SimpleRole> getSimpleRoles(
            FintJwtEndUserPrincipal principal,
            String search,
            List<String> orgUnits,
            String roleType,
            Boolean aggRoles
    ) {
        List<Role> roles;

        if ((orgUnits == null) && !(roleType.equals("ALLTYPES"))) {
            if (aggRoles == null) {
                roles = roleRepository.getRolesByNameTypeAggregated(search, roleType);
            } else {
                roles = roleRepository.getRolesByNameTypeAggregated(search, roleType, aggRoles);
            }
            return roles
                    .stream()
                    .map(Role::toSimpleRole)
                    .toList();
        }

        if ((orgUnits != null) && (roleType.equals("ALLTYPES"))) {
            if (aggRoles == null) {
                roles = roleRepository.getRolesByNameOrgunitsAggregated(search, orgUnits);
            } else {
                roles = roleRepository.getRolesByNameOrgunitsAggregated(search, orgUnits, aggRoles);
            }
            return roles
                    .stream()
                    .map(Role::toSimpleRole)
                    .toList();
        }

        if ((orgUnits == null) && (roleType.equals("ALLTYPES"))) {
            if (aggRoles == null) {
                roles = roleRepository.getRolesByNameAggregated(search);
            } else {
                roles = roleRepository.getRolesByNameAggregated(search, aggRoles);
            }

            return roles
                    .stream()
                    .map(Role::toSimpleRole)
                    .toList();
        }

        if (aggRoles == null) {
            roles = roleRepository.getRolesByNameTypeOrgunitsAggregated(search, roleType, orgUnits);
        } else {
            roles = roleRepository.getRolesByNameTypeOrgunitsAggregated(search, roleType, orgUnits, aggRoles);
        }

        return roles
                .stream()
                .map(Role::toSimpleRole)
                .toList();
    }
}
