package no.fintlabs.member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SimpleMember {
    private Long id;
    private String firstName;
    private String lastName;
    private String userType;
    private String userName;
}
