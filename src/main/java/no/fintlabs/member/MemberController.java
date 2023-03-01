package no.fintlabs.member;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.role.RoleRepository;
import no.fintlabs.role.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/member")

public class MemberController {
    private final MemberResponseFactory memberResponseFactory;
    private final RoleRepository roleRepository;
    private final MemberService memberService;
    private final RoleService roleService;

    public MemberController(MemberResponseFactory memberResponseFactory, MemberService memberService,
                            RoleRepository roleRepository, RoleService roleService) {
        this.memberResponseFactory = memberResponseFactory;
        this.memberService = memberService;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
    }
//    @GetMapping
//    public ResponseEntity<Map<String, Object>> getMembers(@AuthenticationPrincipal Jwt jwt,
//                                                                  @RequestParam(value = "$filter", required = false) String filter,
//                                                                  @RequestParam(defaultValue = "0") int page,
//                                                                  @RequestParam(defaultValue = "${fint.kontroll.role-catalog.pagesize:20}") int size) {
//
//        log.info("Finding all members for role with filter: " + filter + " at page: " + page + " (first page = 0)" );
//
//        return memberResponseFactory.toResponseEntity(
//                //FintJwtEndUserPrincipal.from(jwt),roleId,
//                filter, page, size);
//    }
    @GetMapping("/role/{roleId}")
    public ResponseEntity<Map<String, Object>> getMembersWithRole(@AuthenticationPrincipal Jwt jwt,
                                                        @PathVariable Long roleId,
                                                        @RequestParam(value = "$filter", required = false) String filter,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "${fint.kontroll.role-catalog.pagesize:20}") int size) {

        log.info("Finding all members for role " + roleId + " with filter: " + filter + " at page: " + page + " (first page = 0)" );

        return memberResponseFactory.toResponseEntity(
                //FintJwtEndUserPrincipal.from(jwt),
                roleId, filter, page, size);
    }
//    @GetMapping
//    public Flux<Member> getAllMembers(){
//        log.info("Fetching all members");
//        return memberService.getAllMembers();
//    }
//    @GetMapping("/id/{id}")
//    public Mono<Member> getMemberById(@PathVariable Long id){
//        log.info("Fetching member info for : "+ id.toString());
//        return  memberService.findMemberById(id);
//    }
//    @GetMapping("/username/{userName}")
//    public Mono<Member> getMemberByUserName(@PathVariable String userName){
//        log.info("Fetching member info for : "+ userName);
//        return  memberService.findMemberByUserName(userName);
//    }
//    @GetMapping("/id/{id}/roles")
//    public Flux<RoleDTO> getAllRolesForMemberById(@PathVariable Long id) {
//        return roleService.findRolesByMemberId(id);
//    }
}
