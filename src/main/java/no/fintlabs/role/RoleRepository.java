package no.fintlabs.role;

import no.fintlabs.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByResourceId (String Id);

    Optional<Role> findByRoleId (String roleId);

    Optional<List<Role>> findRolesByMembersId (Long id);

}