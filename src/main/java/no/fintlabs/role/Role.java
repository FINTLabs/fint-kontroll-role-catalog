package no.fintlabs.role;

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
import no.fintlabs.member.Member;
import no.fintlabs.membership.Membership;
import org.hibernate.Hibernate;
import org.hibernate.annotations.NaturalId;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Slf4j
@Entity
@Table(name = "Roles", indexes = @Index(name = "role_id_index", columnList = "roleId"))
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Integer noOfMembers;

    private String roleStatus;
    private Date roleStatusChanged;

    @ToString.Exclude
    @OneToMany(mappedBy = "role", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private Set<Membership> memberships;

    @Transient
    Integer noOfMemberships = memberships == null ? 0 : this.memberships
            .stream()
            .filter(membership -> membership.getMembershipStatus().equals("ACTIVE"))
            .toList()
            .size();

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
                .memberships(noOfMemberships)
                .build();
    }
}
