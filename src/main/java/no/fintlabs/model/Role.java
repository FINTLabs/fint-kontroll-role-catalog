package no.fintlabs.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Entity
@Table(name="Roles")
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
public class Role {
    @Id
    private String resourceId;
    private String roleId;
    private String roleName;
    private String roleType;
    private String roleSubType;
    private boolean aggregatedRole;
    private String roleSource;
}
