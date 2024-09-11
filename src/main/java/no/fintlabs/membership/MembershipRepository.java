package no.fintlabs.membership;

import no.fintlabs.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, MembershipId> {
    @Query("SELECT m.member FROM Membership m WHERE m.role.id = :roleId")
    List<Member> findAllMembersByRoleId(@Param("roleId") Long roleId);
}
