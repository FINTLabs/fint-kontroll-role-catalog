package no.fintlabs.membership;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.Setter;
import no.fintlabs.member.Member;
import no.fintlabs.role.Role;

@Setter
@Embeddable
public class MembershipId implements Serializable{
    private Role role;
    private Member member;

    @ManyToOne(cascade = CascadeType.ALL)
    public Role getRole() {
        return role;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    public Member getMember() {
        return member;
    }
}
