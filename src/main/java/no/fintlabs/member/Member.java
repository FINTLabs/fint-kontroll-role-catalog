package no.fintlabs.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import no.fintlabs.membership.Membership;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "members", indexes = @Index(name = "resource_id_index", columnList = "resourceId"))
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC, force = true)
@Builder
@EqualsAndHashCode
public class Member {

    @Id
    @NonNull
    private Long id;
    private String resourceId;
    private String firstName;
    private String lastName;
    private String userType;
    private String userName;
    private UUID identityProviderUserObjectId;
    private String organisationUnitName;
    private String organisationUnitId;
    private String status;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "member", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private Set<Membership> memberships;

    public SimpleMember toSimpleMember() {
        return SimpleMember
                .builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .userType(userType)
                .userName(userName)
                .build();
    }
}
