package no.fintlabs.membership;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberRepository;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleRepository;
import no.fintlabs.util.MissingReferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void processMembership(KafkaMembership kafkaMembership) {
        Long roleId = kafkaMembership.getRoleId();
        Long memberId = kafkaMembership.getMemberId();

        log.info("Processing membership event. Role: {}, Member: {}, Status: {}, Changed: {}",
                roleId, memberId, kafkaMembership.getMemberStatus(), kafkaMembership.getMemberStatusChanged());

        Role role = getRoleOrThrow(roleId);
        Member member = getMemberOrThrow(memberId);

        MembershipId membershipId = new MembershipId(roleId, memberId);
        Optional<Membership> existingOpt = membershipRepository.findById(membershipId);

        String newStatus = kafkaMembership.getMemberStatus() == null ? "ACTIVE" : kafkaMembership.getMemberStatus();
        Date newChanged = kafkaMembership.getMemberStatusChanged();

        Membership membership = createOrLoadMembership(existingOpt, membershipId, role, member);
        boolean isNew = existingOpt.isEmpty();
        boolean wasActive = "ACTIVE".equalsIgnoreCase(membership.getMembershipStatus());
        boolean nowActive = "ACTIVE".equalsIgnoreCase(newStatus);

        if (isNew || isMembershipChanged(membership, newStatus, newChanged)) {
            log.info("Saving membership update. isNew={}, statusChange={} -> {}, changedAt={}",
                    isNew, membership.getMembershipStatus(), newStatus, newChanged);

            membership.setMembershipStatus(newStatus);
            membership.setMembershipStatusChanged(newChanged);
            membershipRepository.save(membership);

            adjustRoleMemberCountIfNeeded(role, isNew, wasActive, nowActive);
        } else {
            log.debug("No changes detected for membership. Skipping save.");
        }
    }

    private Role getRoleOrThrow(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Role not found: {}", roleId);
                    return new MissingReferenceException("Role not found: " + roleId);
                });
    }

    private Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("Member not found: {}", memberId);
                    return new MissingReferenceException("Member not found: " + memberId);
                });
    }

    private Membership createOrLoadMembership(Optional<Membership> existingOpt, MembershipId id, Role role, Member member) {
        return existingOpt.orElseGet(() -> {
            log.info("New membership detected. Role: {}, Member: {}", role.getId(), member.getId());
            Membership m = new Membership();
            m.setId(id);
            m.setRole(role);
            m.setMember(member);
            return m;
        });
    }

    private boolean isMembershipChanged(Membership membership, String newStatus, Date newChanged) {
        String currentStatus = membership.getMembershipStatus();
        Date currentChanged = membership.getMembershipStatusChanged();

        return !newStatus.equals(currentStatus) ||
                (newChanged != null && currentChanged != null &&
                        newChanged.toInstant().isAfter(currentChanged.toInstant()));
    }

    private void adjustRoleMemberCountIfNeeded(Role role, boolean isNew, boolean wasActive, boolean nowActive) {
        if (isNew || (!wasActive && nowActive)) {
            role.incrementMemberCount();
            roleRepository.save(role);
            log.info("Incremented member count for Role: {} to {}", role.getId(), role.getNoOfMembers());
        }

        if (wasActive && !nowActive) {
            role.decrementMemberCount();
            roleRepository.save(role);
            log.info("Decremented member count for Role: {} to {}", role.getId(), role.getNoOfMembers());
        }
    }
}


