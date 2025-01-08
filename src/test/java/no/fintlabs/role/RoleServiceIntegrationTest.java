package no.fintlabs.role;

import no.fintlabs.DatabaseIntegrationTest;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberRepository;
import no.fintlabs.member.MemberService;
import no.fintlabs.membership.Membership;
import no.fintlabs.membership.MembershipId;
import no.fintlabs.membership.MembershipRepository;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.OpaService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({RoleService.class})
@Testcontainers
public class RoleServiceIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RoleService roleService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MembershipRepository membershipRepository;
    @MockBean
    private OpaService opaService;

    @Test
    public void shouldSaveRoleMemberRelationship() {
        Role role = createRole();

        Member member = createMember();

        Membership membership = new Membership();
        membership.setRole(role);
        membership.setMember(member);
        membership.setMembershipStatus("ACTIVE");

        MembershipId membershipId = new MembershipId();
        membershipId.setMemberId(member.getId());
        membershipId.setRoleId(role.getId());
        membership.setId(membershipId);

        Set<Membership> memberships = new HashSet<>();
        memberships.add(membership);

        role.setMemberships(memberships);
        member.setMemberships(memberships);

        membershipRepository.save(membership);

        Role fetchedRole = roleRepository.findById(role.getId()).orElseThrow();
        List<Membership> foundMemberships = membershipRepository.findAll();

        assertThat(fetchedRole.getMemberships()).hasSize(1);
        assertThat(foundMemberships.get(0).getMember().getFirstName()).isEqualTo(member.getFirstName());
        assertThat(foundMemberships.get(0).getRole().getRoleName()).isEqualTo(role.getRoleName());
        assertThat(foundMemberships.get(0).getMembershipStatus()).isEqualTo("ACTIVE");
    }

    @Test
    public void shouldCreateRoleNotOverrideMemberships() {
        Role role = createRole();
        Member member = createMember();
        Set<Membership> memberships = createMembership(role, member);

        role.setMemberships(memberships);
        member.setMemberships(memberships);


        Role initialSavedRole = roleRepository.findById(role.getId()).orElseThrow();
        assertThat(initialSavedRole.getMemberships()).hasSize(1);

        Role roleChanged = new Role();
        roleChanged.setRoleId("ansatt@digit");
        roleChanged.setResourceId("https://test2.test");
        roleChanged.setRoleName("Ansatt2 - DIGIT Digitaliseringsavdeling");
        roleChanged.setRoleType("ansatt");
        roleChanged.setOrganisationUnitId("40");
        roleChanged.setOrganisationUnitName("DIGIT2 Digitaliseringsavdeling");
        roleChanged.setRoleSource("fint");
        roleChanged.setAggregatedRole(false);
        roleChanged.setNoOfMembers(100);
        roleChanged.setMemberships(Set.of());

        roleService.save(roleChanged);

        Role fetchedRole = roleRepository.findById(role.getId()).orElseThrow();
        List<Membership> foundMemberships = membershipRepository.findAll();

        assertThat(fetchedRole.getMemberships()).hasSize(1);
        assertThat(fetchedRole.getResourceId()).isEqualTo(roleChanged.getResourceId());
        assertThat(fetchedRole.getRoleName()).isEqualTo(roleChanged.getRoleName());
        assertThat(fetchedRole.getOrganisationUnitId()).isEqualTo(roleChanged.getOrganisationUnitId());
        assertThat(fetchedRole.getOrganisationUnitName()).isEqualTo(roleChanged.getOrganisationUnitName());
        assertThat(fetchedRole.getNoOfMembers()).isEqualTo(role.getNoOfMembers());
        assertThat(fetchedRole.getMemberships()).isEqualTo(role.getMemberships());

        assertThat(foundMemberships.get(0).getMember().getFirstName()).isEqualTo(member.getFirstName());
        assertThat(foundMemberships.get(0).getRole().getRoleName()).isEqualTo(role.getRoleName());
        assertThat(foundMemberships.get(0).getMembershipStatus()).isEqualTo("ACTIVE");
    }


    @NotNull
    private Set<Membership> createMembership(Role role, Member member) {
        Membership membership = new Membership();
        membership.setRole(role);
        membership.setMember(member);
        membership.setMembershipStatus("ACTIVE");

        MembershipId membershipId = new MembershipId();
        membershipId.setMemberId(member.getId());
        membershipId.setRoleId(role.getId());
        membership.setId(membershipId);

        Set<Membership> memberships = new HashSet<>();
        memberships.add(membership);
        membershipRepository.save(membership);
        return memberships;
    }

    @NotNull
    private Member createMember() {
        Member member = new Member();
        member.setId(2L);
        member.setFirstName("Ola");
        member.setLastName("Nordmann");
        member.setUserName("ola.nordmann");
        member.setResourceId("https://test.test");
        member = memberRepository.save(member);
        return member;
    }

    @NotNull
    private Role createRole() {
        Role role = new Role();
        role.setRoleId("ansatt@digit");
        role.setResourceId("https://test.test");
        role.setRoleName("Ansatt - DIGIT Digitaliseringsavdeling");
        role.setRoleType("ansatt");
        role.setOrganisationUnitId("36");
        role.setOrganisationUnitName("DIGIT Digitaliseringsavdeling");
        role.setRoleSource("fint");
        role.setAggregatedRole(false);
        role.setNoOfMembers(1);
        role = roleRepository.save(role);
        return role;
    }
}
