package no.fintlabs.role;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailedRole {
    private Long id;
    //private String roleId;
    private String roleName;
    private String roleType;
    private String roleSubType;
    private boolean aggregatedRole;
    private String roleSource;
    private String organisationUnitId;
    private String organisationUnitName;
}
