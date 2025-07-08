package no.fintlabs.role;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.membership.Membership;
import org.hibernate.annotations.NaturalId;

import java.util.Date;
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

    public DetailedRole toDetailedRole() {
        return DetailedRole
                .builder()
                .id(id)
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
                .memberships(noOfMembers)
                .build();
    }

    public void incrementMemberCount() {
        if (this.noOfMembers == null) {
            this.noOfMembers = 1;
        }
        else {
            this.noOfMembers++;
        }
    }

    public void decrementMemberCount() {
        if (this.noOfMembers > 0) {
            this.noOfMembers--;
        }
    }

}
