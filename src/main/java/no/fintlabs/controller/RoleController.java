package no.fintlabs.controller;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.model.Member;
import no.fintlabs.model.Role;
import no.fintlabs.role.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
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
    @PostMapping
    public Mono<Role> createRole(@RequestBody Role role) {
        log.info("Creating new role: " +role.getRoleName());
        return roleService.createNewRole(role);
    }

    @GetMapping("/roleid/{roleId}")
    public Mono<Role> getRoleByRoleId(@PathVariable String roleId){
        log.info("Fetching role info for : "+ roleId);
        return  roleService.findRoleByRoleId(roleId);
    }
}
