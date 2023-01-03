package no.fintlabs.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;

@Data
@Slf4j
@Entity
@Table(name="Roles")
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PUBLIC, force=true)
public class Role {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;
    private String resourceId;
    private String roleName;
    private String roleType;
    private String roleSubType;
    private boolean aggregatedRole;
    private String roleSource;

    @ManyToMany(fetch = FetchType.LAZY,
        cascade = {
            CascadeType.MERGE
        })@JoinTable(name ="Role_Memberships",
    joinColumns = {@JoinColumn(name="role_id")},
    inverseJoinColumns = {@JoinColumn(name="member_id")})
    private Set<Member> members = new HashSet<>();
}
