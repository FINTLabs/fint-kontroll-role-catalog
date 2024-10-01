package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberResponseFactory;
import no.fintlabs.membership.MembershipRepository;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.Scope;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final RoleResponseFactory roleResponseFactory;
    private final MemberResponseFactory memberResponseFactory;
    private final AuthorizationClient authorizationClient;
    private final MembershipRepository membershipRepository;

    public RoleController(RoleService roleService,
                          RoleResponseFactory roleResponseFactory,
                          MemberResponseFactory memberResponseFactory,
                          AuthorizationClient authorizationClient,
                          MembershipRepository membershipRepository
    ) {
        this.roleService = roleService;
        this.roleResponseFactory = roleResponseFactory;
        this.memberResponseFactory = memberResponseFactory;
        this.authorizationClient = authorizationClient;
        this.membershipRepository = membershipRepository;
    }

    private List<String> getOrgUnitsInScope() {

        List<Scope> userScopes = authorizationClient.getUserScopesList();
        log.info("User scopes from api: {}", userScopes);

        return userScopes.stream()
                .filter(scope -> scope.getObjectType().equals("role"))
                .map(Scope::getOrgUnits)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @GetMapping()
    public ResponseEntity<Map<String, Object>> getSimpleRoles(@AuthenticationPrincipal Jwt jwt,
                                                              @RequestParam(value = "search", defaultValue = "%") String search,
                                                              @RequestParam(value = "orgunits", required = false) List<String> orgUnits,
                                                              @RequestParam(value = "roletype", defaultValue = "ALLTYPES") String roleType,
                                                              @RequestParam(value = "aggroles", required = false) Boolean aggRoles,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "${fint.kontroll.role-catalog.pagesize:20}") int size) {

        log.info("search: " + search + "showaggroles: " + aggRoles);

        List<String> orgUnitsInScope = getOrgUnitsInScope();
        log.info("Org units returned from scope: {}", orgUnitsInScope);

        return roleResponseFactory.toResponseEntity(FintJwtEndUserPrincipal.from(jwt), search, orgUnits, orgUnitsInScope, roleType, aggRoles, page, size);

    }

    @GetMapping("{id}")
    public DetailedRole getRoleById(@PathVariable Long id) {
        log.info("Fetching role info for : " + id.toString());
        return roleService.GetDetailedRoleById(id);
    }

    @GetMapping("{id}/members")
    public ResponseEntity<RoleMemberDto> getMembersByRoleId(@AuthenticationPrincipal Jwt jwt,
                                                            @PathVariable Long id,
                                                            @RequestParam(value = "name", required = false, defaultValue = "") String name,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "${fint.kontroll.role-catalog.pagesize:20}") int size) {
        log.info("Fetching members for roleId: {}", id);

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
}
