package no.fintlabs.role;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;
    private final RoleResponseFactory roleResponseFactory;

    public RoleController(RoleService roleService, RoleResponseFactory roleResponseFactory) {
        this.roleService = roleService;
        this.roleResponseFactory = roleResponseFactory;
    }
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRoles(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam(value = "$filter", required = false) String filter,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "${fint.kontroll.role-catalog.pagesize:20}") int size) {

        log.info("Finding roles with filter: " + filter + " at page: " + page + " (first page = 0)" );

        return roleResponseFactory.toResponseEntity(
                //FintJwtEndUserPrincipal.from(jwt),
                filter, page, size);
    }
    @GetMapping("{id}")
    public Mono<DetailedRole> getRoleById(@PathVariable Long id){
        log.info("Fetching role info for : "+ id.toString());
        return  roleService.GetDetailedRoleById(id);
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
