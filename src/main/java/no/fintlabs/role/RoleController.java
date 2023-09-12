package no.fintlabs.role;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.MemberResponseFactory;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.Scope;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final RoleResponseFactory roleResponseFactory;
    private  final MemberResponseFactory memberResponseFactory;
    private final AuthorizationClient authorizationClient;

    public RoleController(RoleService roleService,
                          RoleResponseFactory roleResponseFactory,
                          MemberResponseFactory memberResponseFactory,
                          AuthorizationClient authorizationClient
                          ) {
        this.roleService = roleService;
        this.roleResponseFactory = roleResponseFactory;
        this.memberResponseFactory = memberResponseFactory;
        this.authorizationClient = authorizationClient;
    }

    private List<String> getOrgUnitsInScope() {

        List<Scope> userScopes = authorizationClient.getUserScopes();
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
                                                                   @RequestParam(value = "orgunits", required = false)List<String> orgUnits,
                                                                   @RequestParam(value = "roletype", defaultValue = "ALLTYPES") String roleType,
                                                                   @RequestParam(value = "aggroles",required = false) Boolean aggRoles,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "${fint.kontroll.role-catalog.pagesize:20}") int size ){

        log.info("search: " +search+ "showaggroles: " +aggRoles);

        List<String> orgUnitsInScope = getOrgUnitsInScope();
        log.info("Org units returned from scope: {}", orgUnitsInScope);

        return roleResponseFactory.toResponseEntity(FintJwtEndUserPrincipal.from(jwt),search,orgUnits, orgUnitsInScope, roleType,aggRoles,page,size);

    }

    @GetMapping("{id}")
    public DetailedRole getRoleById(@PathVariable Long id){
        log.info("Fetching role info for : "+ id.toString());
        return  roleService.GetDetailedRoleById(id);
    }


    //ToDo: flyttes fra membercontroller
    @GetMapping("{id}/members")
    public ResponseEntity<Map<String , Object>> getMembersByRoleId(@AuthenticationPrincipal Jwt jwt,
                                                                   @PathVariable Long id,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "${fint.kontroll.role-catalog.pagesize:20}") int size){
        log.info("Fetching members for roleId: " +id);
        return memberResponseFactory.toResponseEntity(id,page,size);
    }


//    @PostMapping
//    public Mono<Role> createRole(@RequestBody Role role) {
//        log.info("Creating new role: " +role.getRoleName());
//        return roleService.createNewRole(role);
//    }
//
//    @GetMapping("/roleid/{roleId}")
//    public Mono<Role> getRoleByRoleId(@PathVariable String roleId){
//        log.info("Fetching role info for : "+ roleId);
//        return  roleService.findRoleByRoleId(roleId);
//    }
}
