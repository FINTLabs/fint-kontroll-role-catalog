package no.fintlabs.controller;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.Role;
import no.fintlabs.role.RoleService;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.HandlerMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    @GetMapping
    public Flux<Role> getAllRoles(){
        log.info("Fetching all roles");
        return roleService.getAllRoles();
    }
    @GetMapping("/id/{id}")
    public Mono<Role> getRoleById(@PathVariable Long id){
        log.info("Fetching role info for : "+ id.toString());
        return  roleService.findRoleById(id);
    }

    @GetMapping("/roleid/{roleId}")
    public Mono<Role> getRoleByRoleId(@PathVariable String roleId){
        log.info("Fetching role info for : "+ roleId);
        return  roleService.findRoleByRoleId(roleId);
    }
    // GET /api/role/resourceid/https://beta.felleskomponent.no.. gives error
    //  org.apache.http.client.ClientProtocolException: URI does not specify a valid host name
    //  ':' is the cause
    @GetMapping("/resourceid/**")
    public Mono<Role> getRoleByResourceId(HttpServletRequest request){
        String resourceId = extractPath(request);
        return  roleService.findRoleByResourceId(resourceId);
    }

    private String extractPath(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String matchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(matchPattern, path);
    }
}
