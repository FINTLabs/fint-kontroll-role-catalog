package no.fintlabs.membership;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.*;
import no.fintlabs.member.Member;
import no.fintlabs.role.Role;

@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@Builder
public class MembershipId implements Serializable {
    private Long roleId;
    private Long memberId;
}
