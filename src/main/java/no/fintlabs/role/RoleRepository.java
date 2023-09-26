package no.fintlabs.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByResourceId (String Id);

    Optional<Role> findByRoleId (String roleId);

    Optional<List<Role>> findRolesByMembersId (Long id);


    @Query("""
            select r from Role r
            where upper(r.roleName) like upper(concat('%', ?1, '%')) 
            and upper(r.roleType) = upper(?2) 
            and r.organisationUnitId in ?3 
            and r.aggregatedRole = ?4""")
    List<Role> getRolesByNameTypeOrgunitsAggregated(
            String roleName,
            String roleType,
            Collection<String> organisationUnitIds,
            boolean aggregatedRole);

    @Query("""
            select r from Role r
            where upper(r.roleName) like upper(concat('%', ?1, '%')) 
            and upper(r.roleType) = upper(?2) 
            and r.organisationUnitId in ?3""")
    List<Role> getRolesByNameTypeOrgunitsAggregated(
            String roleName,
            String roleType,
            Collection<String> organisationUnitIds);

    @Query("""
            select r from Role r
            where upper(r.roleName) like upper(concat('%', ?1, '%')) 
            and upper(r.roleType) = upper(?2) 
            and r.aggregatedRole = ?3""")
    List<Role> getRolesByNameAndTypeAggregated(
            String roleName,
            String roleType,
            boolean aggregatedRole);

    @Query("""
            select r from Role r
            where upper(r.roleName) like upper(concat('%', ?1, '%')) 
            and upper(r.roleType) = upper(?2)""")
    List<Role> getRolesByNameAndTypeAggregated(
            String roleName,
            String roleType);

    @Query("""
            select r from Role r
            where upper(r.roleName) like upper(concat('%', ?1, '%')) 
            and r.organisationUnitId in ?2 
            and r.aggregatedRole = ?3""")
    List<Role> getRolesByNameOrgunitsAggregated(
            String roleName,
            Collection<String> organisationUnitIds,
            boolean aggregatedRole);

    @Query("""
            select r from Role r
            where upper(r.roleName) like upper(concat('%', ?1, '%')) 
            and r.organisationUnitId in ?2 """)
    List<Role> getRolesByNameOrgunitsAggregated(
            String roleName,
            Collection<String> organisationUnitIds);


    @Query("""
            select r from Role r 
            where upper(r.roleName) like upper(concat('%', ?1, '%')) 
            and r.aggregatedRole = ?2""")
    List<Role> getRolesByNameAggregated(
            String roleName,
            boolean aggregatedRole);

    @Query("""
            select r from Role r 
            where upper(r.roleName) like upper(concat('%', ?1, '%')) 
            """)
    List<Role> getRolesByNameAggregated(
            String roleName);











}