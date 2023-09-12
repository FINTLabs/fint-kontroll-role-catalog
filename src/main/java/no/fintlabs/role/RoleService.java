package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
//import no.vigoiks.resourceserver.security.FintJwtEndRolePrincipal;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.OrgUnitType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleService {

    private RoleRepository roleRepository;

    private MemberService memberService;
    private AuthorizationClient authorizationClient;

    public RoleService(AuthorizationClient authorizationClient, RoleRepository roleRepository, MemberService memberService) {
        this.authorizationClient = authorizationClient;
        this.roleRepository = roleRepository;
        this.memberService = memberService;
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
        List<String> orgUnitsInSearch = getOrgUnitsInSearch(orgUnits, orgUnitsInScope);

        if (orgUnitsInSearch.contains(OrgUnitType.ALLORGUNITS.name())) {
            if (roleType.equals("ALLTYPES")) {
                if (aggRoles==null) {
                    roles = roleRepository.getRolesByNameAggregated(search);
                }
                else {
                    roles =roleRepository.getRolesByNameAggregated(search, aggRoles);
                }
            }
            else {
                if (aggRoles == null) {
                    roles = roleRepository.getRolesByNameAndTypeAggregated(search, roleType);
                }
                else {
                    roles = roleRepository.getRolesByNameAndTypeAggregated(search, roleType, aggRoles);
                }
            }
        }
        else {
            if (roleType.equals("ALLTYPES")) {
                if (aggRoles == null) {
                    roles = roleRepository.getRolesByNameOrgunitsAggregated(search, orgUnitsInSearch);
                }
                else {
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
        }
        List<SimpleRole> simpleRoles = roles.stream()
                .map(Role::toSimpleRole)
                .toList();
        return simpleRoles;
    }

    private static List<String> getOrgUnitsInSearch(List<String> orgUnits, List<String> orgUnitsInScope) {
        List<String> orgUnitsInSearch;

        if (orgUnits ==null) {
            orgUnitsInSearch = orgUnitsInScope;
            log.info("OrgUnits parameter is empty, using orgunits from scope {} in search", orgUnitsInScope);
        }
        else {
            log.info("OrgUnits parameter list: {}", orgUnits);

            if (orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name())) {
                orgUnitsInSearch = orgUnits;
            }
            else {
                orgUnitsInSearch = orgUnits.stream()
                        .filter(orgUnitsInScope::contains)
                        .collect(Collectors.toList());
            }
            log.info("OrgUnits in search: {}", orgUnitsInSearch);
        }
        return orgUnitsInSearch;
    }
}
