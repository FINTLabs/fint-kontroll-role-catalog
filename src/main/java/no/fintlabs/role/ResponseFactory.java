package no.fintlabs.controller;

import no.fint.antlr.FintFilterService;
import no.fintlabs.repository.RoleRepository;
import no.fintlabs.model.Role;
import no.vigoiks.resourceserver.security.FintJwtEndUserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ResponseFactory {
    private final FintFilterService fintFilterService;
    private final RoleRepository roleRepository;



    public ResponseFactory(FintFilterService fintFilterService, RoleRepository roleRepository) {
        this.fintFilterService = fintFilterService;
        this.roleRepository = roleRepository;
    }


    public ResponseEntity<Map<String, Object>> toResponseEntity(
            //FintJwtEndRolePrincipal principal,
            String filter,
            int page,
            int size) {
        Stream<Role> roleStream = roleRepository.findAll().stream();
        ResponseEntity<Map<String, Object>> entity = toResponseEntity(
                toPage(
                        StringUtils.hasText(filter)
                                ? fintFilterService
                                .from(roleStream, filter)
                                .map(Role::toSimpleRole).toList()
                                : roleStream.map(Role::toSimpleRole).toList(),
                        PageRequest.of(page, size)
                )
        );

        return entity;
    }

    private Page<SimpleRole> toPage(List<SimpleRole> list, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), list.size());

        return start > list.size()
                ? new PageImpl<>(new ArrayList<>(), paging, list.size())
                : new PageImpl<>(list.subList(start, end), paging, list.size());
    }

    public ResponseEntity<Map<String, Object>> toResponseEntity(Page<SimpleRole> rolePage) {

        return new ResponseEntity<>(
                Map.of("totalItems", rolePage.getTotalElements(),
                        "roles", rolePage.getContent(),
                        "currentPage", rolePage.getNumber(),
                        "totalPages", rolePage.getTotalPages()
                ),
                HttpStatus.OK
        );
    }
}
