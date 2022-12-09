package no.fintlabs.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Entity
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
public class Role {
    @Id
    private String roleid;
    private String resourceid;
    private String rolename;
    private String roletype;
    private String rolesubtype;
    private boolean aggregatedrole;
    private String rolesource;

}
