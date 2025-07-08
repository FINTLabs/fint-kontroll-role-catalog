package no.fintlabs.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoleResponseFactory {
    public static Page<SimpleRole> toPage(List<SimpleRole> list, Pageable paging) {
        int start = (int) paging.getOffset();
        int end = Math.min((start + paging.getPageSize()), list.size());

        return start > list.size()
                ? new PageImpl<>(new ArrayList<>(), paging, list.size())
                : new PageImpl<>(list.subList(start, end), paging, list.size());
    }

    public static ResponseEntity<Map<String, Object>> toResponseEntity(Page<SimpleRole> rolePage) {

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
