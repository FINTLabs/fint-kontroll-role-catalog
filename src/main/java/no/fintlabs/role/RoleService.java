package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
//import no.vigoiks.resourceserver.security.FintJwtEndRolePrincipal;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipService;
import no.fintlabs.roleCatalogRole.RoleCatalogRoleService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.OrgUnitType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleService {

    private RoleRepository roleRepository;

    private FintCache<String, Role> roleCache;

    private MemberService memberService;
    private RoleCatalogRoleService roleCatalogRoleService;
    private RoleCatalogMembershipService roleCatalogMembershipService;
    private AuthorizationClient authorizationClient;

    public RoleService(AuthorizationClient authorizationClient, RoleRepository roleRepository, FintCache<String, Role> roleCache, MemberService memberService, RoleCatalogRoleService roleCatalogRoleService, RoleCatalogMembershipService roleCatalogMembershipService) {
        this.authorizationClient = authorizationClient;
        this.roleRepository = roleRepository;
        this.roleCache = roleCache;
        this.memberService = memberService;
        this.roleCatalogRoleService = roleCatalogRoleService;
        this.roleCatalogMembershipService = roleCatalogMembershipService;
    }

    public Role save(Role role) {

        String roleId = role.getRoleId();
        Set<Member> members = role.getMembers();
        log.info("Save  members for role {} started", members.size(), roleId);
        members.forEach(member -> memberService.save(member)
        );
        log.info("Save {} members for role {} finished", members.size(), roleId);
        Optional<Role> existingRole = roleRepository.findByRoleId(roleId);

        Role persistedRole;
        if (existingRole.isEmpty()) {
            log.info("Role {} not found. Saving new role", roleId);
        } else {
            log.info("Role {} already exists", roleId);
            role.setId(existingRole.get().getId());
            log.info("Updating existing role {}", roleId);
        }
        persistedRole =  roleRepository.save(role);
        log.info("Save/update role {} finished", roleId);
        return persistedRole;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll().stream().collect(Collectors.toList());
    }
    public List<Role> getAllRolesFromCache() {
        return roleCache.getAllDistinct();
    }
    public Role createNewRole(Role role) {
        return roleRepository.save(role);
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
                .orElse(new DetailedRole());
    }

    public List<SimpleRole> getSimpleRoles(
            FintJwtEndUserPrincipal principal,
            String search,
            List<String> orgUnits,
            List<String> orgUnitsInScope,
            String roleType,
            Boolean aggRoles
    ) {
        List<String> orgUnitsInSearch = getOrgUnitsInSearch(orgUnits, orgUnitsInScope);
        List<Role> roles = getRoles(search, roleType, aggRoles, orgUnitsInSearch);

        return roles.stream()
                .map(Role::toSimpleRole)
                .toList();
    }

    private List<Role> getRoles(String search, String roleType, Boolean aggRoles, List<String> orgUnitsInSearch) {
        List<Role> roles;
        if (orgUnitsInSearch.contains(OrgUnitType.ALLORGUNITS.name())) {
            if (roleType.equals("ALLTYPES")) {
                if (aggRoles == null) {
                    return roleRepository.getRolesByNameAggregated(search);
                }
                return roleRepository.getRolesByNameAggregated(search, aggRoles);
            }
            if (aggRoles == null) {
                return roleRepository.getRolesByNameAndTypeAggregated(search, roleType);
            }
            return roleRepository.getRolesByNameAndTypeAggregated(search, roleType, aggRoles);
        }
        if (roleType.equals("ALLTYPES")) {
            if (aggRoles == null) {
                return roleRepository.getRolesByNameOrgunitsAggregated(search, orgUnitsInSearch);
            }
            return roleRepository.getRolesByNameOrgunitsAggregated(search, orgUnitsInSearch, aggRoles);
        }
        if (aggRoles == null) {
            return roleRepository.getRolesByNameTypeOrgunitsAggregated(search, roleType, orgUnitsInSearch);
        }
        return roleRepository.getRolesByNameTypeOrgunitsAggregated(search, roleType, orgUnitsInSearch, aggRoles);
    }


    public List<String> getOrgUnitsInSearch(List<String> orgUnits, List<String> orgUnitsInScope) {

        if (orgUnits == null) {
            log.info("OrgUnits parameter is empty, using orgunits from scope {} in search", orgUnitsInScope);
            return orgUnitsInScope;
        }
        log.info("OrgUnits parameter list: {}", orgUnits);

        if (orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name())) {
            return orgUnits;
        }
        List<String> filteredOrgUnits = orgUnits.stream()
                .filter(orgUnitsInScope::contains)
                .collect(Collectors.toList());

        log.info("OrgUnits in search: {}", filteredOrgUnits);
        return filteredOrgUnits;
    }
}
