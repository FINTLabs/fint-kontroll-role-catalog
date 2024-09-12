package no.fintlabs.membership;

import jakarta.persistence.*;
import lombok.*;
import no.fintlabs.member.Member;
import no.fintlabs.role.Role;

@Entity
@Table(name= "role_memberships")
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PUBLIC, force=true)
@Getter
@Setter
@Builder
public class Membership {
    @EmbeddedId
    private MembershipId id;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private Member member;

    @Setter
    @Getter
    @Builder.Default
    private boolean isActive = true;
}
