package no.fintlabs.member;

import no.fintlabs.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT m from Member m " +
           "INNER JOIN m.roles roles " +
           "WHERE roles.id = :id " +
           "AND (:name IS NULL OR :name = '' OR " +
           "CONCAT(UPPER(m.firstName), ' ', UPPER(m.lastName)) LIKE UPPER(CONCAT('%', :name, '%'))) " +
           "ORDER BY m.firstName, m.lastName"
    )
    Page<Member> getMembersByRoleId(Long id, String name, Pageable pageable);








}
