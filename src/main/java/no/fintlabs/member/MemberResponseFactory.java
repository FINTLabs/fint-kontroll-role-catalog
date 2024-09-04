package no.fintlabs.member;

import no.fint.antlr.FintFilterService;
import no.fintlabs.role.RoleRepository;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class MemberResponseFactory {
    private final FintFilterService fintFilterService;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    public MemberResponseFactory(FintFilterService fintFilterService, MemberRepository memberRepository,
                                 RoleRepository roleRepository) {
        this.fintFilterService = fintFilterService;
        this.memberRepository = memberRepository;
        this.roleRepository = roleRepository;
    }
    public ResponseEntity<Map<String, Object>> toResponseEntity(
            //FintJwtEndRolePrincipal principal,
            Long roleId,
            String filter,
            int page,
            int size
    ) {
        Stream<Member> memberStream = memberRepository.getAllByRolesId(roleId).stream();
        //Stream<Member> memberStream = memberRepository.findAll().stream();
        ResponseEntity<Map<String, Object>> entity = toResponseEntity(
                toPage(
                        StringUtils.hasText(filter)
                                ? fintFilterService
                                .from(memberStream, filter)
                                .map(Member::toSimpleMember).toList()
                                : memberStream.map(Member::toSimpleMember).toList(),
                        PageRequest.of(page, size)
                )
        );

        return entity;
    }

    private Page<SimpleMember> toPage(List<SimpleMember> list, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), list.size());

        return start > list.size()
                ? new PageImpl<>(new ArrayList<>(), paging, list.size())
                : new PageImpl<>(list.subList(start, end), paging, list.size());
    }

    public ResponseEntity<Map<String, Object>> toResponseEntity(Page<SimpleMember> memberPage) {

        return new ResponseEntity<>(
                Map.of( "members", memberPage.getContent(),
                        "currentPage", memberPage.getNumber(),
                        "totalPages", memberPage.getTotalPages(),
                        "size", memberPage.getSize(),
                        "totalItems", memberPage.getTotalElements()
                ),
                HttpStatus.OK
        );
    }
}

