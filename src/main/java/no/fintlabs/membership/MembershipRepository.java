package no.fintlabs.membership;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, MembershipId> {

}
