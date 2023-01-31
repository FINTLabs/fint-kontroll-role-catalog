package no.fintlabs.controller;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.dto.RoleDTO;
import no.fintlabs.member.MemberService;
import no.fintlabs.model.Member;
import no.fintlabs.model.Role;
import no.fintlabs.repository.RoleRepository;
import no.fintlabs.role.RoleService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/member")

public class MemberController {
    private final RoleRepository roleRepository;
    private final MemberService memberService;
    private final RoleService roleService;

    public MemberController(MemberService memberService,
                            RoleRepository roleRepository, RoleService roleService) {
        this.memberService = memberService;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
    }
    @GetMapping
    public Flux<Member> getAllMembers(){
        log.info("Fetching all members");
        return memberService.getAllMembers();
    }
    @GetMapping("/id/{id}")
    public Mono<Member> getMemberById(@PathVariable Long id){
        log.info("Fetching member info for : "+ id.toString());
        return  memberService.findMemberById(id);
    }
    @GetMapping("/username/{userName}")
    public Mono<Member> getMemberByUserName(@PathVariable String userName){
        log.info("Fetching member info for : "+ userName);
        return  memberService.findMemberByUserName(userName);
    }
    @GetMapping("/id/{id}/roles")
    public Flux<RoleDTO> getAllRolesForMemberById(@PathVariable Long id) {
        return roleService.findRolesByMemberId(id);
    }
}
