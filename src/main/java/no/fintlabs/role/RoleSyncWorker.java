package no.fintlabs.role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.membership.MembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleSyncWorker {

    private final RoleRepository roleRepository;
    private final MembershipRepository membershipRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void recomputeOneRole(Long roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow();
        int currentCount = role.getNoOfMembers() == null ? 0 : role.getNoOfMembers();
        int newCount   = membershipRepository.getActiveMembersCountByRoleId(roleId);
        if (currentCount != newCount) {
            log.info("Updated active member count. roleId={}, oldCount={}, newCount={}", role.getRoleId(), currentCount, newCount);
            role.setNoOfMembers(newCount);
            roleRepository.save(role);
        } else {
            log.debug("Active member count unchanged. roleId={}, count={}", role.getRoleId(), currentCount);
        }
    }
}
