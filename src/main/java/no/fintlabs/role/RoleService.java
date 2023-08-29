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

import java.util.*;
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
        String roleId = role.getRoleId();
        Optional<Role> existingRole = roleRepository.findByRoleId(roleId);

        if (existingRole.isPresent()) {
            log.info("Role {} already exists", roleId);
            return existingRole.get();
        }
        else {
            log.info("Trying to save role {}", roleId);
            return roleRepository.save(role);
        }
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
        List<String> orgUnitsInSearch;
        HashSet<String> accessibleOrgUnits = getAccessibleOrgUnitsFromOPA();

        log.info("Accessible orgunits from OPA: {}",accessibleOrgUnits);

        if (orgUnits==null) {
            orgUnitsInSearch = accessibleOrgUnits.stream().toList();
            log.info("OrgUnits parameter is empty, ");
        }
        else {
            log.info("OrgUnits parameter list: {}", orgUnits);
            orgUnitsInSearch = orgUnits.stream()
                    .filter(accessibleOrgUnits::contains)
                    .collect(Collectors.toList());
            log.info("OrgUnits in search: {}", orgUnitsInSearch);
        }

        if (roleType.equals("ALLTYPES")) {
            if (aggRoles == null) {
                roles = roleRepository.getRolesByNameOrgunitsAggregated(search, orgUnitsInSearch);
            } else {
                roles = roleRepository.getRolesByNameOrgunitsAggregated(search, orgUnitsInSearch, aggRoles);
            }
        }
        else {
            if (aggRoles == null) {
                roles = roleRepository.getRolesByNameTypeOrgunitsAggregated(search, roleType, orgUnitsInSearch);
            } else {
                roles = roleRepository.getRolesByNameTypeOrgunitsAggregated(search, roleType, orgUnitsInSearch, aggRoles);
            }
        }
        return roles
                .stream()
                .map(Role::toSimpleRole)
                .toList();
    }

    private HashSet<String> getAccessibleOrgUnitsFromOPA() {
        return new HashSet<>(Arrays.asList("198","205","211","218"));
    }
}
