package no.fintlabs.role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SimpleRole {
    private Long id;
    private String roleName;
    private String roleType;
    private String roleSubType;
    private boolean aggregatedRole;
    private String organisationUnitId;
    private String organisationUnitName;
    private Integer memberships;
}
