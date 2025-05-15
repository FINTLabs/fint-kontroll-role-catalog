package no.fintlabs.roleCatalogMembership;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleCatalogMembership {
    private String id;
    private Long roleId;
    private Long memberId;
    private UUID identityProviderUserObjectId;
    private String memberStatus;
    private Date membershipStatusChanged;
}
