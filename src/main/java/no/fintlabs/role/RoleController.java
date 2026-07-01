package no.fintlabs.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.Member;
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
    @GetMapping("/syncnoofmembers")
    public void syncNoOfMembers() {
        roleService.syncNoOfMembers();
        log.info("Triggered active member count sync for all roles");
    }

    @OnlyDevelopers
    @GetMapping("/publishallroles")
    public void publishallroles() {
        roleCatalogPublishingComponent.publishRoles();
        log.info("Triggered role catalog publish for all roles");
    }

    @OnlyDevelopers
    @GetMapping("/publishrole/{id}")
    public void publishrole(@PathVariable Long id) {
        Role roleToPublish = roleService.getRoleByRoleId(id);
        roleCatalogPublishingComponent.publishRole(roleToPublish);
        log.info("Triggered role catalog publish. id={}, roleId={}", id, roleToPublish.getRoleId());
    }

    @OnlyDevelopers
    @GetMapping("/publishallmemberships")
    public void publishallmemberships() {
        roleCatalogMembershipPublishingComponent.publishMemberships();
        log.info("Triggered role catalog membership publish for all roles");
    }

    @OnlyDevelopers
    @GetMapping("/publishmembershipsforrole/{id}")
    public void publishMembershipsForRole(@PathVariable Long id){
        Role roleToPublish = roleService.getRoleByRoleId(id);

        roleCatalogMembershipPublishingComponent.publishMembershipsForRole(roleToPublish);
        log.info("Triggered role catalog membership publish. id={}, roleId={}", id, roleToPublish.getRoleId());
    }

}
