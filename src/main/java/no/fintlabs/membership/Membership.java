package no.fintlabs.membership;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import no.fintlabs.member.Member;
import no.fintlabs.role.Role;

@Entity
@Table(name= "role_memberships")
@AssociationOverrides({
        @AssociationOverride(name = "primaryKey.role",
                joinColumns = @JoinColumn(name = "id")),
        @AssociationOverride(name = "primaryKey.member",
                joinColumns = @JoinColumn(name = "id"))
})
public class Membership {
    private MembershipId primaryKey = new MembershipId();
    @Setter
    @Getter
    private boolean membershipStatus;

    @EmbeddedId
    public MembershipId getPrimaryKey() {
        return primaryKey;
    }
//    public void setPrimaryKey(MembershipId primaryKey) {
//        this.primaryKey = primaryKey;
//    }
    @Transient
    public Role getRole() {
        return getPrimaryKey().getRole();
    }
    public void setRole(Role role) {
        getPrimaryKey().setRole(role);
    }
    @Transient
    public Member getMember() {
        return getPrimaryKey().getMember();
    }
    public void setMember(Member member) {
        getPrimaryKey().setMember(member);
    }
}
