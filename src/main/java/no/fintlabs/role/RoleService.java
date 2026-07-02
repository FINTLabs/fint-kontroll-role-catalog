package no.fintlabs.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.OrgUnitType;
import no.fintlabs.maintenance.MaintenanceStatusUpdateResult;
import no.fintlabs.membership.Membership;
import no.fintlabs.membership.MembershipRepository;
import no.fintlabs.opa.OpaService;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogPublishingComponent;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final OpaService opaService;
    private final RoleSyncWorker worker;
    private final RoleCatalogPublishingComponent roleCatalogPublishingComponent;
    private final MembershipRepository membershipRepository;
    private final RoleCatalogMembershipPublishingComponent roleCatalogMembershipPublishingComponent;

    private static final String INACTIVE = "INACTIVE";

    public Page<Role> findBySearchCriteria(
            String searchString,
            List<String> filteredOrgUnits,
            List<String> validOrgUnits,
            List<String> roleTypes,
            Boolean getAggregatedRoles,
            Pageable pageable
    ) {
        List<String> orgUnitsInScope = opaService.getOrgUnitsInScope("role");
        log.debug("Loaded {} org units from role scope", orgUnitsInScope.size());

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
            log.debug("Creating role. roleId={}, status={}", roleId, role.getRoleStatus());
            role.setNoOfMembers(0);
            role.setRoleStatusChanged(Date.from(Instant.now()));
            persistedRole = roleRepository.save(role);
            roleCatalogPublishingComponent.publishRole(role);
        } else {
            role.setId(existingRole.get().getId());
            Role mappedRole = mapChangesToExistingRole(role, existingRole.get());
            log.debug("Updating role. roleId={}, status={} -> {}", roleId, existingRole.get().getRoleStatus(), role.getRoleStatus());
            persistedRole = roleRepository.save(mappedRole);
        }

        return persistedRole;
    }

    private Role mapChangesToExistingRole(Role incomingRole, Role existingRole) {
        Date roleStatusChanged = getStatusChangedDate(
                existingRole.getRoleStatus(),
                incomingRole.getRoleStatus(),
                existingRole.getRoleStatusChanged()
        );
        existingRole.setResourceId(incomingRole.getResourceId());
        existingRole.setRoleId(incomingRole.getRoleId());
        existingRole.setRoleStatus(incomingRole.getRoleStatus());
        existingRole.setRoleStatusChanged(roleStatusChanged);
        existingRole.setRoleName(incomingRole.getRoleName());
        existingRole.setRoleType(incomingRole.getRoleType());
        existingRole.setRoleSubType(incomingRole.getRoleSubType());
        existingRole.setAggregatedRole(incomingRole.isAggregatedRole());
        existingRole.setRoleSource(incomingRole.getRoleSource());
        existingRole.setOrganisationUnitId(incomingRole.getOrganisationUnitId());
        existingRole.setOrganisationUnitName(incomingRole.getOrganisationUnitName());
        existingRole.setStartDate(incomingRole.getStartDate());
        existingRole.setEndDate(incomingRole.getEndDate());
        return existingRole;
    }

    private Date getStatusChangedDate(String currentStatus, String newStatus, Date currentStatusChanged) {
        if (!isSameStatus(currentStatus, newStatus)) {
            return Date.from(Instant.now());
        }
        return currentStatusChanged;
    }

    private boolean isSameStatus(String firstStatus, String secondStatus) {
        return firstStatus.equalsIgnoreCase(secondStatus);
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
            log.debug("No org unit filter supplied; using {} scoped org units", orgUnitsInScope.size());
            return orgUnitsInScope;
        }
        log.debug("Filtering roles by {} requested org units", orgUnits.size());

        if (orgUnitsInScope.contains(OrgUnitType.ALLORGUNITS.name())) {
            return orgUnits;
        }
        List<String> filteredOrgUnits = orgUnits.stream()
                .filter(orgUnitsInScope::contains)
                .collect(Collectors.toList());

        log.debug("Role search org unit filter reduced to {} scoped org units", filteredOrgUnits.size());
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

    public Role getRoleByRoleId(Long id) {
        return roleRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("No role with id " + id + " found"));
    }

    @Transactional
    public MaintenanceStatusUpdateResult expireRolesAndMemberships(
            boolean dryRun
    ) {
        Date referenceDate = Date.from(Instant.now());
        List<Role> expiredRoles = roleRepository.findExpiredRoles(referenceDate);
        List<Role> rolesToUpdate = expiredRoles.stream()
                .filter(role -> !INACTIVE.equalsIgnoreCase(role.getRoleStatus()) || hasActiveMemberCount(role))
                .toList();
        List<Membership> membershipsToUpdate = expiredRoles.stream()
                .map(Role::getMemberships)
                .filter(Objects::nonNull)
                .flatMap(Set::stream)
                .filter(membership -> !INACTIVE.equalsIgnoreCase(membership.getMembershipStatus()))
                .toList();

        if (dryRun) {
            log.info("Dry run for expired role maintenance. expiredRoles={}, rolesToUpdate={}, membershipsToUpdate={}",
                    expiredRoles.size(), rolesToUpdate.size(), membershipsToUpdate.size());
            return new MaintenanceStatusUpdateResult(
                    true,
                    referenceDate,
                    "expired-roles-and-memberships",
                    expiredRoles.size(),
                    membershipsToUpdate.size(),
                    0,
                    0,
                    0,
                    0,
                    "Dry run only. Set dryRun=false to update and republish."
            );
        }

        rolesToUpdate.forEach(role -> {
            if (!INACTIVE.equalsIgnoreCase(role.getRoleStatus())) {
                role.setRoleStatus(INACTIVE);
                role.setRoleStatusChanged(referenceDate);
            }
            role.setNoOfMembers(0);
            roleRepository.save(role);
            roleCatalogPublishingComponent.publishRole(role);
        });

        membershipsToUpdate.forEach(membership -> {
            membership.setMembershipStatus(INACTIVE);
            membership.setMembershipStatusChanged(referenceDate);
            membershipRepository.save(membership);
            roleCatalogMembershipPublishingComponent.publishMembership(membership);
        });

        log.info("Expired role maintenance completed. expiredRoles={}, rolesUpdated={}, membershipsUpdated={}",
                expiredRoles.size(), rolesToUpdate.size(), membershipsToUpdate.size());
        return new MaintenanceStatusUpdateResult(
                false,
                referenceDate,
                "expired-roles-and-memberships",
                expiredRoles.size(),
                membershipsToUpdate.size(),
                rolesToUpdate.size(),
                membershipsToUpdate.size(),
                rolesToUpdate.size(),
                membershipsToUpdate.size(),
                "Expired roles and their memberships were set to INACTIVE and republished."
        );
    }

    private boolean hasActiveMemberCount(Role role) {
        return role.getNoOfMembers() != null && role.getNoOfMembers() > 0;
    }
}
