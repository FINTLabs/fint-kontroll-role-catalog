package no.fintlabs.membership;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberRepository;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleRepository;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipEntityProducerService;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipPublishingComponent;
import no.fintlabs.util.MissingReferenceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static no.fintlabs.roleCatalogMembership.RoleCatalogMembershipService.getRoleCatalogMembershipId;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;

    private static final String ACTIVE = "ACTIVE";
    private final RoleCatalogMembershipEntityProducerService roleCatalogMembershipEntityProducerService;
    private final RoleCatalogMembershipPublishingComponent roleCatalogMembershipPublishingComponent;

    @Transactional
    public void processMembership(KafkaMembership kafkaMembership) {
        Long roleId = kafkaMembership.getRoleId();
        Long memberId = kafkaMembership.getMemberId();

        log.info("Processing membership event. Role: {}, Member: {}, Status: {}, Start: {}, End: {}",
                roleId, memberId, kafkaMembership.getMemberStatus(), kafkaMembership.getStartDate(), kafkaMembership.getEndDate());

        Role role = getRoleOrThrow(roleId);
        Member member = getMemberOrThrow(memberId);

        MembershipId membershipId = new MembershipId(roleId, memberId);
        Optional<Membership> existingMembership = membershipRepository.findById(membershipId);

        String newStatus = kafkaMembership.getMemberStatus() == null ? ACTIVE : kafkaMembership.getMemberStatus();
        boolean isNew = existingMembership.isEmpty();

        Membership membership = createOrLoadMembership(existingMembership, membershipId, role, member);
        boolean wasActive = ACTIVE.equalsIgnoreCase(membership.getMembershipStatus());
        boolean nowActive = ACTIVE.equalsIgnoreCase(newStatus);

        if (isNew || isMembershipChanged(membership, kafkaMembership, newStatus)) {
            Date newChangedDate = getStatusChangedDate(
                    membership.getMembershipStatus(),
                    newStatus,
                    membership.getMembershipStatusChanged()
            );
            log.info("Saving membership update. isNew={}, statusChange={} -> {}, changedAt={}",
                    isNew, membership.getMembershipStatus(), newStatus, newChangedDate);

            membership.setMembershipStatus(newStatus);
            membership.setMembershipStatusChanged(newChangedDate);
            membership.setStartDate(kafkaMembership.getStartDate());
            membership.setEndDate(kafkaMembership.getEndDate());
            membershipRepository.save(membership);

            roleCatalogMembershipPublishingComponent.publishMembership(membership);

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

    private Membership createOrLoadMembership(Optional<Membership> existingMembership, MembershipId id, Role role, Member member) {
        return existingMembership.orElseGet(() -> {
            log.info("New membership detected. Role: {}, Member: {}", role.getId(), member.getId());
            Membership m = new Membership();
            m.setId(id);
            m.setRole(role);
            m.setMember(member);
            return m;
        });
    }

    private boolean isMembershipChanged(Membership membership, KafkaMembership kafkaMembership, String newStatus) {
        String currentStatus = membership.getMembershipStatus();

        return !Objects.equals(newStatus, currentStatus)
                || !Objects.equals(membership.getStartDate(), kafkaMembership.getStartDate())
                || !Objects.equals(membership.getEndDate(), kafkaMembership.getEndDate());
    }

    private Date getStatusChangedDate(String currentStatus, String newStatus, Date currentStatusChanged) {
        if (!Objects.equals(currentStatus, newStatus)) {
            return Date.from(Instant.now());
        }
        return currentStatusChanged;
    }

    private void adjustRoleMemberCountIfNeeded(Role role, boolean isNew, boolean wasActive, boolean nowActive) {
        if ((isNew && nowActive) || (!isNew && !wasActive && nowActive)) {
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
    @Transactional
    public void removeAllMembershipsForUser(Member member) {
        List<Membership> activeMemberships = membershipRepository.findAllByMember_Id(member.getId());
        if (!activeMemberships.isEmpty()) {
            log.info("Removing all memberships for member {}. Found {} active memberships", member.getId(), activeMemberships.size());
        }
        activeMemberships.forEach(this::deleteMembership);
    }

    private void deleteMembership(Membership membership) {
        log.info("Deleting membership: {}", membership.getId());
        String kafkaKey = getRoleCatalogMembershipId(membership.getRole(), membership);
        membershipRepository.delete(membership);
        adjustRoleMemberCountIfNeeded(membership.getRole(), false, true, false);
        roleCatalogMembershipEntityProducerService.publishTombstone(kafkaKey);
    }

}

