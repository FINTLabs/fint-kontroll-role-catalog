package no.fintlabs.role;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.OrgUnitType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import no.fintlabs.opa.OpaService;

@Service
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final OpaService opaService;

    public RoleService(RoleRepository roleRepository, OpaService opaService) {
        this.roleRepository = roleRepository;
        this.opaService = opaService;
    }

    public Page<Role> findBySearchCriteria(
            String searchString,
            List<String> filteredOrgUnits,
            List<String> roleTypes,
            Boolean getAggregatedRoles,
            Pageable pageable
    ) {
        List<String> orgUnitsInScope = opaService.getOrgUnitsInScope("role");
        log.info("Org units returned from scope: {}", orgUnitsInScope);

        RoleSpecificationBuilder roleSpecificationBuilder = new RoleSpecificationBuilder(
                searchString,
                filteredOrgUnits,
                orgUnitsInScope,
                roleTypes,
                getAggregatedRoles
        );
        return roleRepository
                .findAll(roleSpecificationBuilder.build(), pageable);
    }

    @Transactional
    public Role save(Role role) {

        String roleId = role.getRoleId();

        //TODO: Change this to getMemberships and then saveAll(memberships)
        // Members should be obtained by consuming the kontrolluser topic and saved separately
//        Set<Member> members = role.getMembers();
//        log.info("Save {} members for role {} started", members.size(), roleId);
//        memberService.saveAll(members);
//        log.info("Save {} members for role {} finished", members.size(), roleId);
       Optional<Role> existingRole = roleRepository.findByRoleId(roleId);

        Role persistedRole;

        if (existingRole.isEmpty()) {
            log.info("Role {} not found. Saving new role", roleId);
            persistedRole =  roleRepository.save(role);
        } else {
            log.info("Role {} already exists", roleId);
            role.setId(existingRole.get().getId());
            Role mappedRole = mapChangesToExistingRole(role, existingRole.get());
            log.info("Updating existing role {}", roleId);
            persistedRole =  roleRepository.save(mappedRole);
        }

        log.info("Save/update role {} finished", roleId);

        return persistedRole;
    }

    private Role mapChangesToExistingRole(Role incomingRole, Role existingRole) {
        existingRole.setResourceId(incomingRole.getResourceId());
        existingRole.setRoleId(incomingRole.getRoleId());
        existingRole.setRoleStatus(incomingRole.getRoleStatus());
        existingRole.setRoleStatusChanged(incomingRole.getRoleStatusChanged());
        existingRole.setRoleName(incomingRole.getRoleName());
        existingRole.setRoleType(incomingRole.getRoleType());
        existingRole.setRoleSubType(incomingRole.getRoleSubType());
        existingRole.setAggregatedRole(incomingRole.isAggregatedRole());
        existingRole.setRoleSource(incomingRole.getRoleSource());
        existingRole.setOrganisationUnitId(incomingRole.getOrganisationUnitId());
        existingRole.setOrganisationUnitName(incomingRole.getOrganisationUnitName());
        return existingRole;
    }

    public List<Role> getAllRoles() {
        return new ArrayList<>(roleRepository.findAll());
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

    public List<Role> getRolesByParams(
            String search,
            String roleType,
            Boolean aggRoles,
            List<String> orgUnits,
            List<String> orgUnitsInScope
    ) {
        List<String> orgUnitsInSearch = getOrgUnitsInSearch(orgUnits, orgUnitsInScope);

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
    public Integer getNoOfActiveMemberships(Role role) {
        Integer noOfActiveMemberships = role.getMemberships()
                .stream()
                .filter(membership -> membership.getMembershipStatus() != null && membership.getMembershipStatus().equals("ACTIVE"))
                .toList()
                .size();
        log.info("Role {} ({})  has {} active memberships", role.getId(), role.getRoleName(), noOfActiveMemberships);
        return noOfActiveMemberships;
    }
}
