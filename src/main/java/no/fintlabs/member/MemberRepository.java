package no.fintlabs.member;

import no.fintlabs.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    List<Member> getAllByRolesId(Long roleId);
}
