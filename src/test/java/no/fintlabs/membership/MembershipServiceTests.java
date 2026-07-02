package no.fintlabs.membership;

import no.fintlabs.member.Member;
import no.fintlabs.member.MemberRepository;
import no.fintlabs.role.Role;
import no.fintlabs.role.RoleRepository;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipEntityProducerService;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogPublishingComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class MembershipServiceTests {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RoleCatalogMembershipEntityProducerService roleCatalogMembershipEntityProducerService;

    @Mock
    private RoleCatalogMembershipPublishingComponent roleCatalogMembershipPublishingComponent;

    @Mock
    private RoleCatalogPublishingComponent roleCatalogPublishingComponent;

    @InjectMocks
    private MembershipService membershipService;

    @Test
    void shouldDropMembershipWhenStatusIsNull() {
        KafkaMembership kafkaMembership = KafkaMembership.builder()
                .roleId(1L)
                .memberId(2L)
                .build();

        membershipService.processMembership(kafkaMembership);

        verifyNoInteractions(roleRepository, memberRepository, membershipRepository, roleCatalogMembershipPublishingComponent);
    }

    @Test
    void shouldCalculateStatusChangedWhenStatusChanges() {
        Role role = Role.builder().id(1L).resourceId("http://test.no").roleStatus("ACTIVE").noOfMembers(1).build();
        Member member = Member.builder().id(2L).build();
        MembershipId membershipId = new MembershipId(role.getId(), member.getId());
        Date oldStatusChanged = Date.from(Instant.parse("2025-01-01T00:00:00Z"));
        Date startDate = Date.from(Instant.parse("2026-01-01T00:00:00Z"));
        Date endDate = Date.from(Instant.parse("2026-12-31T00:00:00Z"));
        Membership existingMembership = Membership.builder()
                .id(membershipId)
                .role(role)
                .member(member)
                .membershipStatus("ACTIVE")
                .membershipStatusChanged(oldStatusChanged)
                .build();
        KafkaMembership kafkaMembership = KafkaMembership.builder()
                .roleId(role.getId())
                .memberId(member.getId())
                .memberStatus("INACTIVE")
                .startDate(startDate)
                .endDate(endDate)
                .build();

        given(roleRepository.findById(role.getId())).willReturn(Optional.of(role));
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(membershipRepository.findById(membershipId)).willReturn(Optional.of(existingMembership));

        membershipService.processMembership(kafkaMembership);

        ArgumentCaptor<Membership> membershipCaptor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(membershipCaptor.capture());
        Membership savedMembership = membershipCaptor.getValue();
        assertThat(savedMembership.getMembershipStatus()).isEqualTo("INACTIVE");
        assertThat(savedMembership.getMembershipStatusChanged()).isNotEqualTo(oldStatusChanged);
        assertThat(savedMembership.getStartDate()).isEqualTo(startDate);
        assertThat(savedMembership.getEndDate()).isEqualTo(endDate);
    }

    @Test
    void shouldPreserveStatusChangedWhenOnlyDatesChange() {
        Role role = Role.builder().id(1L).resourceId("http://test.no").roleStatus("ACTIVE").noOfMembers(1).build();
        Member member = Member.builder().id(2L).build();
        MembershipId membershipId = new MembershipId(role.getId(), member.getId());
        Date oldStatusChanged = Date.from(Instant.parse("2025-01-01T00:00:00Z"));
        Date startDate = Date.from(Instant.parse("2026-01-01T00:00:00Z"));
        Membership existingMembership = Membership.builder()
                .id(membershipId)
                .role(role)
                .member(member)
                .membershipStatus("ACTIVE")
                .membershipStatusChanged(oldStatusChanged)
                .startDate(Date.from(Instant.parse("2025-01-01T00:00:00Z")))
                .build();
        KafkaMembership kafkaMembership = KafkaMembership.builder()
                .roleId(role.getId())
                .memberId(member.getId())
                .memberStatus("ACTIVE")
                .startDate(startDate)
                .build();

        given(roleRepository.findById(role.getId())).willReturn(Optional.of(role));
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(membershipRepository.findById(membershipId)).willReturn(Optional.of(existingMembership));

        membershipService.processMembership(kafkaMembership);

        ArgumentCaptor<Membership> membershipCaptor = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(membershipCaptor.capture());
        Membership savedMembership = membershipCaptor.getValue();
        assertThat(savedMembership.getMembershipStatusChanged()).isEqualTo(oldStatusChanged);
        assertThat(savedMembership.getStartDate()).isEqualTo(startDate);
    }

    @Test
    void shouldDryRunExpiredMembershipsWithoutSavingOrPublishing() {
        Role role = Role.builder().id(1L).roleId("student-role").resourceId("http://test.no").roleStatus("ACTIVE").noOfMembers(1).build();
        Member member = Member.builder().id(2L).userType("STUDENT").build();
        Membership membership = Membership.builder()
                .id(new MembershipId(role.getId(), member.getId()))
                .role(role)
                .member(member)
                .membershipStatus("ACTIVE")
                .endDate(Date.from(Instant.parse("2025-01-01T00:00:00Z")))
                .build();

        given(membershipRepository.findExpiredActiveMemberships(org.mockito.ArgumentMatchers.any(Date.class), org.mockito.ArgumentMatchers.eq("STUDENT")))
                .willReturn(List.of(membership));

        var result = membershipService.expireMemberships("STUDENT", true);

        assertThat(result.dryRun()).isTrue();
        assertThat(result.matchedMemberships()).isEqualTo(1);
        assertThat(membership.getMembershipStatus()).isEqualTo("ACTIVE");
        verifyNoInteractions(roleCatalogMembershipPublishingComponent, roleCatalogPublishingComponent);
    }

    @Test
    void shouldExpireMembershipsAndRepublishAffectedRole() {
        Role role = Role.builder().id(1L).roleId("student-role").resourceId("http://test.no").roleStatus("ACTIVE").noOfMembers(1).build();
        Member member = Member.builder().id(2L).userType("STUDENT").build();
        Membership membership = Membership.builder()
                .id(new MembershipId(role.getId(), member.getId()))
                .role(role)
                .member(member)
                .membershipStatus("ACTIVE")
                .endDate(Date.from(Instant.parse("2025-01-01T00:00:00Z")))
                .build();

        given(membershipRepository.findExpiredActiveMemberships(org.mockito.ArgumentMatchers.any(Date.class), org.mockito.ArgumentMatchers.isNull()))
                .willReturn(List.of(membership));

        var result = membershipService.expireMemberships(null, false);

        assertThat(result.updatedMemberships()).isEqualTo(1);
        assertThat(result.republishedRoles()).isEqualTo(1);
        assertThat(membership.getMembershipStatus()).isEqualTo("INACTIVE");
        assertThat(membership.getMembershipStatusChanged()).isNotNull();
        assertThat(role.getNoOfMembers()).isZero();
        verify(membershipRepository).save(membership);
        verify(roleRepository).save(role);
        verify(roleCatalogMembershipPublishingComponent).publishMembership(membership);
        verify(roleCatalogPublishingComponent).publishRole(role);
    }
}
