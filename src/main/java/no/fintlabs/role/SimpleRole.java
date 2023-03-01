package no.fintlabs.role;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SimpleRole {
    private Long id;
    //private String roleId;
    private String roleName;
    private String roleType;
    private boolean aggregatedRole;
    private String organisationUnitId;
    private String organisationUnitName;
}
