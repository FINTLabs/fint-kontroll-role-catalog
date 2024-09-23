package no.fintlabs.roleCatalogRole;

import java.util.Date;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class RoleCatalogRole {
    private Long id;
    private String roleId;
    private String roleName;
    private Integer noOfMembers;
    private String roleType;
    private String roleSubType;
    private boolean aggregatedRole;
    private String roleSource;
    private String organisationUnitId;
    private String organisationUnitName;
    private String roleStatus;
    private Date roleStatusChanged;
}
