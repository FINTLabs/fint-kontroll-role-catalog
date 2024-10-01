package no.fintlabs.membership;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.*;
import no.fintlabs.member.Member;
import no.fintlabs.role.Role;

@Setter
@Getter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@Builder
public class MembershipId implements Serializable {
    private Long roleId;
    private Long memberId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MembershipId that = (MembershipId) o;
        return Objects.equals(roleId, that.roleId) && Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, memberId);
    }
}
