package no.fintlabs.membership;

import no.fintlabs.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, MembershipId> {
    @Query("SELECT m.member FROM Membership m WHERE m.role.id = :roleId")
    List<Member> findAllMembersByRoleId(@Param("roleId") Long roleId);


    @Query("select count(m) from Membership m where m.role.id = :roleId and m.membershipStatus = 'ACTIVE'")
    int getActiveMembersCountByRoleId(@Param("roleId") Long roleId);


    @Query("SELECT m.member FROM Membership m " +
           "WHERE m.role.id = :id " +
           "AND (:name IS NULL OR :name = '' OR " +
           "CONCAT(UPPER(m.member.firstName), ' ', UPPER(m.member.lastName)) LIKE UPPER(CONCAT('%', :name, '%'))) " +
           "AND m.membershipStatus = 'ACTIVE' " +
           "ORDER BY m.member.firstName, m.member.lastName"
    )
    Page<Member> getMembersByRoleId(@Param("id") Long id,
                                    @Param("name") String name,
                                    Pageable pageable);

    List<Membership> findAllByMember_Id(Long memberId);

    @Query("""
            select m from Membership m
            join fetch m.role
            join fetch m.member
            where (
                (m.endDate is not null and m.endDate < :referenceDate)
                or (:expireMissingDates = true and m.startDate is null and m.endDate is null)
            )
            and upper(m.membershipStatus) = 'ACTIVE'
            """)
    List<Membership> findExpiredActiveMemberships(
            @Param("referenceDate") Date referenceDate,
            @Param("expireMissingDates") boolean expireMissingDates
    );

    @Query("""
            select m from Membership m
            join fetch m.role
            join fetch m.member
            where (
                (m.endDate is not null and m.endDate < :referenceDate)
                or (:expireMissingDates = true and m.startDate is null and m.endDate is null)
            )
            and upper(m.membershipStatus) = 'ACTIVE'
            and upper(m.member.userType) = :memberUserType
            """)
    List<Membership> findExpiredActiveMembershipsByMemberUserType(
            @Param("referenceDate") Date referenceDate,
            @Param("memberUserType") String memberUserType,
            @Param("expireMissingDates") boolean expireMissingDates
    );

}
