package no.fintlabs.role;

import no.fint.antlr.FintFilterService;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class RoleResponseFactory {
    private final FintFilterService fintFilterService;
    private final RoleRepository roleRepository;
    private final RoleService roleService;

    public RoleResponseFactory(FintFilterService fintFilterService, RoleRepository roleRepository, RoleService roleService) {
        this.fintFilterService = fintFilterService;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
    }

    public ResponseEntity<Map<String, Object>> toResponseEntity(FintJwtEndUserPrincipal principal,
                                                                String search,
                                                                List<String> orgUnits,
                                                                List<String> orgUnitsInScope,
                                                                String roleType,
                                                                Boolean aggRoles,
                                                                int page,
                                                                int size
    ){
        List<SimpleRole> simpleRoles = roleService.getSimpleRoles(principal,search,orgUnits,orgUnitsInScope, roleType,aggRoles);

        return toResponseEntity(toPage(simpleRoles,PageRequest.of(page,size)));
    }


//    public Mono<ResponseEntity<Map<String, Object>>> toResponseEntity(
//            //FintJwtEndRolePrincipal principal,
//            String filter,
//            int page,
//            int size) {
//        Stream<Role> roleStream = roleRepository.findAll().stream();
//        ResponseEntity<Map<String, Object>> entity = toResponseEntity(
//                toPage(
//                       StringUtils.hasText(filter)
//                            ? fintFilterService
//                            .from(roleStream, filter)
//                            .map(Role::toSimpleRole).toList()
//                            : roleStream.map(Role::toSimpleRole).toList(),
//                        PageRequest.of(page, size)
//                )
//                );
//
//        return Mono.just(entity);
//    }

    private Page<SimpleRole> toPage(List<SimpleRole> list, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), list.size());

        return start > list.size()
                ? new PageImpl<>(new ArrayList<>(), paging, list.size())
                : new PageImpl<>(list.subList(start, end), paging, list.size());
    }

    public ResponseEntity<Map<String, Object>> toResponseEntity(Page<SimpleRole> rolePage) {

        return new ResponseEntity<>(
                Map.of(
                        "roles", rolePage.getContent(),
                        "currentPage", rolePage.getNumber(),
                        "totalPages", rolePage.getTotalPages(),
                        "size", rolePage.getSize(),
                        "totalItems", rolePage.getTotalElements()
                ),
                HttpStatus.OK
        );
    }


}
