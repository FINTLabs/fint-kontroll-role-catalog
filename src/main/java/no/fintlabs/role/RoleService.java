package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
//import no.vigoiks.resourceserver.security.FintJwtEndRolePrincipal;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import no.fintlabs.opa.AuthorizationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MemberService memberService;
    private AuthorizationClient authorizationClient;

    public RoleService(AuthorizationClient authorizationClient) {
        this.authorizationClient = authorizationClient;
    }

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

    public List<Role> getAllRoles() {
        return roleRepository.findAll().stream().collect(Collectors.toList());
    }

    public Mono<Role> createNewRole(Role role) {
        Role newRole = roleRepository.save(role);
        return Mono.just(newRole);
    }

    public Role findRoleById(Long id) {
        return roleRepository.findById(id).orElse(new Role());
    }

    public Role findRoleByRoleId(String roleId) {
        return roleRepository.findByRoleId(roleId).orElse(new Role());
    }

    public Role findRoleByResourceId(String id) {
        return roleRepository.findByResourceId(id).orElse(new Role());

    }

    public DetailedRole GetDetailedRoleById(Long id) {
        return roleRepository.findById(id)
                .map(Role::toDetailedRole)
                .orElse(new DetailedRole()
        );
    }

    public List<SimpleRole> getSimpleRoles(
            FintJwtEndUserPrincipal principal,
            String search,
            List<String> orgUnits,
            List<String> orgUnitsInScope,
            String roleType,
            Boolean aggRoles
    ) {
        List<Role> roles;
        List<String> orgUnitsInSearch;

        if (orgUnits==null) {
            orgUnitsInSearch = orgUnitsInScope;
            log.info("OrgUnits parameter is empty, using orgunits from scope {} in search", orgUnitsInScope);
        }
        else {
            log.info("OrgUnits parameter list: {}", orgUnits);
            orgUnitsInSearch = orgUnits.stream()
                    .filter(orgUnitsInScope::contains)
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
        List<SimpleRole> simpleRoles = roles.stream()
                .map(Role::toSimpleRole)
                .toList();
        return simpleRoles;
    }
}
