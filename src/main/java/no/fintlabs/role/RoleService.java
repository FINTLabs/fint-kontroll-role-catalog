package no.fintlabs.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.OrgUnitType;
import no.fintlabs.opa.OpaService;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final OpaService opaService;
    private final RoleSyncWorker worker;

    public Page<Role> findBySearchCriteria(
            String searchString,
            List<String> filteredOrgUnits,
            List<String> validOrgUnits,
            List<String> roleTypes,
            Boolean getAggregatedRoles,
            Pageable pageable
    ) {
        List<String> orgUnitsInScope = opaService.getOrgUnitsInScope("role");
        log.info("Org units returned from scope: {}", orgUnitsInScope);

        List<String> validOrgUnitsInScope = getOrgUnitsValidAndInScope(orgUnitsInScope, validOrgUnits);

        RoleSpecificationBuilder roleSpecificationBuilder = new RoleSpecificationBuilder(
                searchString,
                filteredOrgUnits,
                validOrgUnitsInScope,
                roleTypes,
                getAggregatedRoles
        );
        return roleRepository
                .findAll(roleSpecificationBuilder.build(), pageable);
    }

    @Transactional
    public Role save(Role role) {

        String roleId = role.getRoleId();

        Optional<Role> existingRole = roleRepository.findByRoleId(roleId);

        Role persistedRole;

        if (existingRole.isEmpty()) {
            log.info("Role {} not found. Saving new role", roleId);
            role.setNoOfMembers(0);
            persistedRole = roleRepository.save(role);
        } else {
            log.info("Role {} already exists", roleId);
            role.setId(existingRole.get().getId());
            Role mappedRole = mapChangesToExistingRole(role, existingRole.get());
            log.info("Updating existing role {}", roleId);
            persistedRole = roleRepository.save(mappedRole);
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

    public DetailedRole getDetailedRoleById(Long id) {
        return roleRepository.findById(id)
                .map(Role::toDetailedRole)
                .orElseThrow(() -> new ResourceNotFoundException("No role with id " + id + " found"));
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


    public void syncNoOfMembers() {
        List<Long> ids = roleRepository.findAll().stream().map(Role::getId).toList();
        ids.forEach(worker::recomputeOneRole);
    }

    public static List<String> getOrgUnitsValidAndInScope(List<String> orgUnitsInScope, List<String> validOrgUnits) {
        log.debug("Getting intersection of {} and {}", orgUnitsInScope,  validOrgUnits);
        if (validOrgUnits ==null || validOrgUnits.isEmpty()) {
            log.debug("No valid orgUnits found, returning org units in scope");
            return orgUnitsInScope;
        }
        if (orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name())) {
            log.debug("org unit scope contains ALLORGUNITS, returning valid orgUnits");
            return validOrgUnits;
        }
        List<String> intersection = new ArrayList<>(orgUnitsInScope);
        intersection.retainAll(validOrgUnits);
        log.debug("Both orgUnitsInScope and validOrgUnits are non empty subsets. Returning the actual intersection");
        return intersection;
    }
}
