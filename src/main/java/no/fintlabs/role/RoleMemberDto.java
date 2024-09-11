package no.fintlabs.role;

import lombok.Builder;
import lombok.Getter;
import no.fintlabs.member.SimpleMember;

import java.util.List;

@Builder
@Getter
public class RoleMemberDto {
    private long totalItems;
    private int totalPages;
    private int currentPage;
    private int size;
    private List<SimpleMember> members;
}
