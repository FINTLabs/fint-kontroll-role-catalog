package no.fintlabs.role;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.maintenance.MaintenanceStatusUpdateResult;
import no.fintlabs.member.Member;
import no.fintlabs.membership.MembershipService;
import no.fintlabs.membership.MembershipRepository;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.Scope;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogPublishingComponent;
import no.fintlabs.util.OnlyDevelopers;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final AuthorizationClient authorizationClient;
    private final MembershipRepository membershipRepository;
    private final MembershipService membershipService;
    private final RoleCatalogMembershipPublishingComponent roleCatalogMembershipPublishingComponent;
    private final RoleCatalogPublishingComponent roleCatalogPublishingComponent;

    @GetMapping("/old")
    public ResponseEntity<Map<String, Object>> getRoles(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam(value = "search", defaultValue = "%") String search,
                                                        @RequestParam(value = "orgunits", required = false) List<String> orgUnits,
                                                        @RequestParam(value = "roletype", defaultValue = "ALLTYPES") String roleType,
                                                        @RequestParam(value = "aggroles", required = false) Boolean aggRoles,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "${fint.kontroll.role-catalog.pagesize:20}") int size) {

        List<String> orgUnitsInScope = getOrgUnitsInScope();
        log.debug("Legacy role search. search={}, roleType={}, requestedOrgUnits={}, scopedOrgUnits={}, aggregated={}",
                search, roleType, orgUnits, orgUnitsInScope.size(), aggRoles);

        PageRequest pageRequest = PageRequest.of(page, size);

        List<Role> rolesByParams = roleService.getRolesByParams(search, roleType, aggRoles, orgUnits, orgUnitsInScope);

        List<SimpleRole> simpleRoles = rolesByParams.stream()
                .map(Role::toSimpleRole)
                .collect(Collectors.toList());

        return RoleResponseFactory.toResponseEntity(RoleResponseFactory.toPage(simpleRoles, pageRequest));

    }
    @GetMapping()
    public ResponseEntity<Map<String, Object>> getRolesV1(
            @RequestParam(value = "search", required = false) String searchName,
            @RequestParam(value = "orgunits", required = false) List<String>  requestedOrgUnits,
            @RequestParam(value = "validorgunits", required = false) List<String> validOrgUnits,
            @RequestParam(value = "roletype", required = false) List<String> roleTypes,
            @RequestParam(value = "aggroles", required = false) Boolean aggRoles,
            @SortDefault(sort = {"roleName"}, direction = Sort.Direction.ASC)
            @ParameterObject @PageableDefault(size = 100) Pageable pageable
    ) {
        log.debug("Role search. search={}, requestedOrgUnits={}, validOrgUnits={}, roleTypes={}, aggregated={}, page={}, size={}",
                searchName, requestedOrgUnits, validOrgUnits, roleTypes, aggRoles, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Role> rolesByParams = roleService.findBySearchCriteria(searchName, requestedOrgUnits, validOrgUnits, roleTypes, aggRoles, pageable);
            return ResponseEntity.ok(RoleMapper.toRoleDtoPage(rolesByParams));
        } catch (Exception e) {
            log.warn("Role search failed. search={}, requestedOrgUnits={}, validOrgUnits={}, roleTypes={}, aggregated={}",
                    searchName, requestedOrgUnits, validOrgUnits, roleTypes, aggRoles, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong when fetching roles");
        }
    }

    @GetMapping("{id}")
    public DetailedRole getRoleById(@PathVariable Long id) {
        log.debug("Fetching role details. roleId={}", id);
        return roleService.getDetailedRoleById(id);
    }

    @GetMapping("{id}/members")
    public ResponseEntity<RoleMemberDto> getMembersByRoleId(@AuthenticationPrincipal Jwt jwt,
                                                            @PathVariable Long id,
                                                            @RequestParam(value = "name", required = false, defaultValue = "") String name,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "${fint.kontroll.role-catalog.pagesize:20}") int size) {
        log.debug("Fetching role members. roleId={}, nameFilter={}, page={}, size={}", id, name, page, size);

        Pageable pageable = Pageable.ofSize(size).withPage(page);

        Page<Member> members = membershipRepository.getMembersByRoleId(id, name, pageable);

        RoleMemberDto mappedMembers = RoleMemberDto.builder()
                .members(members.getContent().stream().map(Member::toSimpleMember).collect(Collectors.toList()))
                .totalItems(members.getTotalElements())
                .totalPages(members.getTotalPages())
                .currentPage(members.getNumber())
                .size(members.getSize())
                .build();

        return ResponseEntity.ok(mappedMembers);
    }

    private List<String> getOrgUnitsInScope() {

        List<Scope> userScopes = authorizationClient.getUserScopesList();
        log.debug("Loaded {} authorization scopes for role request", userScopes.size());

        return userScopes.stream()
                .filter(scope -> scope.getObjectType().equals("role"))
                .map(Scope::getOrgUnits)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @OnlyDevelopers
    @Operation(
            tags = {"Maintenance endpoints"},
            summary = "Synchronize role member counts",
            description = "Developer-only operation that recalculates and stores the active member count for every role."
    )
    @GetMapping("/syncnoofmembers")
    public void syncNoOfMembers() {
        roleService.syncNoOfMembers();
        log.info("Triggered active member count sync for all roles");
    }

    @OnlyDevelopers
    @Operation(
            tags = {"Maintenance endpoints"},
            summary = "Publish all roles",
            description = "Developer-only operation that publishes every role from the role catalog to the downstream role catalog topic."
    )
    @GetMapping("/publishallroles")
    public void publishallroles() {
        roleCatalogPublishingComponent.publishRoles();
        log.info("Triggered role catalog publish for all roles");
    }

    @OnlyDevelopers
    @Operation(
            tags = {"Maintenance endpoints"},
            summary = "Publish one role",
            description = "Developer-only operation that publishes a single role from the role catalog to the downstream role catalog topic."
    )
    @GetMapping("/publishrole/{id}")
    public void publishrole(
            @Parameter(description = "Internal database id of the role to publish.", required = true)
            @PathVariable Long id
    ) {
        Role roleToPublish = roleService.getRoleByRoleId(id);
        roleCatalogPublishingComponent.publishRole(roleToPublish);
        log.info("Triggered role catalog publish. id={}, roleId={}", id, roleToPublish.getRoleId());
    }

    @OnlyDevelopers
    @Operation(
            tags = {"Maintenance endpoints"},
            summary = "Publish all memberships",
            description = "Developer-only operation that publishes all role memberships to the downstream role catalog membership topic."
    )
    @GetMapping("/publishallmemberships")
    public void publishallmemberships() {
        roleCatalogMembershipPublishingComponent.publishMemberships();
        log.info("Triggered role catalog membership publish for all roles");
    }

    @OnlyDevelopers
    @Operation(
            tags = {"Maintenance endpoints"},
            summary = "Publish memberships for one role",
            description = "Developer-only operation that publishes all memberships connected to a single role to the downstream role catalog membership topic."
    )
    @GetMapping("/publishmembershipsforrole/{id}")
    public void publishMembershipsForRole(
            @Parameter(description = "Internal database id of the role whose memberships should be published.", required = true)
            @PathVariable Long id
    ){
        Role roleToPublish = roleService.getRoleByRoleId(id);

        roleCatalogMembershipPublishingComponent.publishMembershipsForRole(roleToPublish);
        log.info("Triggered role catalog membership publish. id={}, roleId={}", id, roleToPublish.getRoleId());
    }

    @OnlyDevelopers
    @Operation(
            tags = {"Maintenance endpoints"},
            summary = "Expire student memberships",
            description = "Developer-only maintenance operation that finds expired student memberships and marks them inactive. Run with dryRun=true to preview the impact without saving changes or publishing updates."
    )
    @PostMapping("/maintenance/expire-student-memberships")
    public MaintenanceStatusUpdateResult expireStudentMemberships(
            @Parameter(description = "When true, calculate and return the expected changes without persisting or publishing them.", example = "true")
            @RequestParam(defaultValue = "true") boolean dryRun,
            @Parameter(description = "When true, also expire active records where both startDate and endDate are empty.", example = "false")
            @RequestParam(defaultValue = "false") boolean expireMissingDates
    ) {
        log.info("Triggered expired student membership maintenance. dryRun={}, expireMissingDates={}", dryRun, expireMissingDates);
        return membershipService.expireMemberships("STUDENT", dryRun, expireMissingDates);
    }

    @OnlyDevelopers
    @Operation(
            tags = {"Maintenance endpoints"},
            summary = "Expire all memberships",
            description = "Developer-only maintenance operation that finds expired memberships for all member types and marks them inactive. Run with dryRun=true to preview the impact without saving changes or publishing updates."
    )
    @PostMapping("/maintenance/expire-memberships")
    public MaintenanceStatusUpdateResult expireMemberships(
            @Parameter(description = "When true, calculate and return the expected changes without persisting or publishing them.", example = "true")
            @RequestParam(defaultValue = "true") boolean dryRun,
            @Parameter(description = "When true, also expire active records where both startDate and endDate are empty.", example = "false")
            @RequestParam(defaultValue = "false") boolean expireMissingDates
    ) {
        log.info("Triggered expired membership maintenance. dryRun={}, expireMissingDates={}", dryRun, expireMissingDates);
        return membershipService.expireMemberships(null, dryRun, expireMissingDates);
    }

    @OnlyDevelopers
    @Operation(
            tags = {"Maintenance endpoints"},
            summary = "Expire roles and memberships",
            description = "Developer-only maintenance operation that finds expired roles, marks them inactive, expires their memberships, and republishes affected data. Run with dryRun=true to preview the impact without saving changes or publishing updates."
    )
    @PostMapping("/maintenance/expire-roles-and-memberships")
    public MaintenanceStatusUpdateResult expireRolesAndMemberships(
            @Parameter(description = "When true, calculate and return the expected changes without persisting or publishing them.", example = "true")
            @RequestParam(defaultValue = "true") boolean dryRun,
            @Parameter(description = "When true, also expire active roles where both startDate and endDate are empty.", example = "false")
            @RequestParam(defaultValue = "false") boolean expireMissingDates
    ) {
        log.info("Triggered expired role maintenance. dryRun={}, expireMissingDates={}", dryRun, expireMissingDates);
        return roleService.expireRolesAndMemberships(dryRun, expireMissingDates);
    }

}
