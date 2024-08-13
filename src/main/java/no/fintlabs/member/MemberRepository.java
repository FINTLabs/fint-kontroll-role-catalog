package no.fintlabs.member;

import no.fintlabs.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    List<Member> getAllByRolesId(Long roleId);

    Member getMemberByResourceId(String resourceId);

    Collection<Member> findByRoles_RoleId(String roleId);

    @Query("select m from Member m inner join m.memberships memberships where memberships.primaryKey.role = ?1")
    List<Member> getMembersByRoleId(Long id);








}
