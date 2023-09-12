package no.fintlabs.role;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.Member;
import org.hibernate.Hibernate;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Slf4j
@Entity
@Table(name="Roles", indexes = @Index(name = "role_id_index",columnList = "roleId"))
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PUBLIC, force=true)
@Builder
public class Role {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;
    @NaturalId
    @Column(nullable = false, unique = true)
    private String roleId;
    @NonNull
    private String resourceId;
    private String roleName;
    private String roleType;
    private String roleSubType;
    private boolean aggregatedRole;
    private String roleSource;
    @Column
    private String organisationUnitId;
    @Column
    private String organisationUnitName;

    @ManyToMany(fetch = FetchType.LAZY,
        cascade = {
            CascadeType.MERGE
//                CascadeType.PERSIST
        })
    @JoinTable(name ="Role_Memberships",
        joinColumns = {@JoinColumn(name="role_id")},
        inverseJoinColumns = {@JoinColumn(name="member_id")})

    @ToString.Exclude
    private Set<Member> members = new HashSet<>();

    public void addMember(Member member) {
        this.members.add(member);
        member.getRoles().add(this);
    }

    public void removeMember(Long memberid) {
        Member member = this.members
                .stream()
                .filter(m -> m.getId()==memberid)
                .findFirst()
                .orElse(null);

        if (member != null)
        {
            this.members.remove(member);
            member.getRoles().remove(this);
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Role role = (Role) o;
        return (id != null && Objects.equals(id, role.id)) || (roleId != null && Objects.equals(roleId, role.roleId));

    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    public DetailedRole toDetailedRole() {
        return DetailedRole
                .builder()
                .id(id)
                //.roleId(roleId)
                .roleName(roleName)
                .roleType(roleType)
                .roleSubType(roleSubType)
                .roleSource(roleSource)
                .aggregatedRole(aggregatedRole)
                .organisationUnitId(organisationUnitId)
                .organisationUnitName(organisationUnitName)
                .build();
    }
    public SimpleRole toSimpleRole() {
        return SimpleRole
                .builder()
                .id(id)
                .roleName(roleName)
                .roleType(roleType)
                .roleSubType(roleSubType)
                .aggregatedRole(aggregatedRole)
                .organisationUnitId(organisationUnitId)
                .organisationUnitName(organisationUnitName)
                .build();
    }
}
