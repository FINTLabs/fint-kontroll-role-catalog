package no.fintlabs.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.membership.Membership;
import no.fintlabs.role.Role;
import org.hibernate.Hibernate;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Slf4j
@Entity
@Table(name="members", indexes = @Index(name = "resource_id_index",columnList = "resourceId"))
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PUBLIC, force=true)
@Builder
public class Member {
    @Id
    @NonNull
    //@GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    private String resourceId;
    private String firstName;
    private String lastName;
    private String userType;
    private String userName;
    private UUID identityProviderUserObjectId;
    private String organisationUnitName;
    private String organisationUnitId;

    private Set<Membership> memberships = new HashSet<>();

    @OneToMany(mappedBy ="primaryKey.member", cascade = CascadeType.ALL)
    public Set<Membership> getMemberships() {
        return memberships;
    }
    public void addMembership(Membership membership) {
        this.memberships.add(membership);
    }

//    @ManyToMany(fetch = FetchType.LAZY,
//            cascade = {
//                CascadeType.PERSIST,
//                CascadeType.MERGE
//            },
//            mappedBy = "members")
//    @JsonIgnore
//    @ToString.Exclude
//    private Set<Role> roles = new HashSet<>();
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
//        Member member = (Member) o;
//        return id != null && Objects.equals(id, member.id);
//    }
//
//    @Override
//    public int hashCode() {
//        return getClass().hashCode();
//    }
//
//
//    public SimpleMember toSimpleMember() {
//        return SimpleMember
//                .builder()
//                .id(id)
//                .firstName(firstName)
//                .lastName(lastName)
//                .userType(userType)
//                .userName(userName)
//                .build();
//    }
}
