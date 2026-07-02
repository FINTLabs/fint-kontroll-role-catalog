package no.fintlabs.role;

import no.fintlabs.OrgUnitType;
import no.fintlabs.member.Member;
import no.fintlabs.membership.Membership;
import no.fintlabs.membership.MembershipId;
import no.fintlabs.membership.MembershipRepository;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipPublishingComponent;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipService;
import no.fintlabs.roleCatalogRole.RoleCatalogPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTests {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleCatalogRoleService roleCatalogRoleService;
    @Mock
    private RoleCatalogMembershipService roleCatalogMembershipService;
    @Mock
    private RoleCatalogPublishingComponent roleCatalogPublishingComponent;
    @Mock
    private RoleCatalogMembershipPublishingComponent roleCatalogMembershipPublishingComponent;
    @Mock
    private MembershipRepository membershipRepository;
    @InjectMocks
    private RoleService roleService;
    private Role role, aggrole;

    private static Role createNewRole() {
        return Role.builder()
                .id(2L)
                .roleId("ansatt@digit-fagtj")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/47")
                .roleName("Ansatt - DIGIT Fagtjenester")
                .roleSource("fint")
                .roleType("ansatt")
                .roleStatus("ACTIVE")
                .aggregatedRole(false)
                .build();
    }

    @BeforeEach
    public void setUp() {
        role = Role.builder()
                .id(1L)
                .roleId("ansatt@digit")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/36")
                .roleName("Ansatt - DIGIT Digitaliseringsavdeling")
                .roleSource("fint")
                .roleType("ansatt")
                .roleStatus("ACTIVE")
                .aggregatedRole(false)
                .memberships(new HashSet<>())
                .build();

        aggrole = Role.builder()
                .id(3L)
                .roleId("ansatt@digit-aggr")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/36")
                .roleName("Ansatt - DIGIT Digitaliseringsavdeling -aggregert")
                .roleSource("fint")
                .roleType("ansatt")
                .roleStatus("ACTIVE")
                .aggregatedRole(false)
                .memberships(new HashSet<>())
                .build();

    }

    @DisplayName("Test for saveRole - save existing role")
    @Test
    public void givenRoleObject_whenSaveExistingRole_thenReturnUpdatedRoleObject() {

        Role roleFromKafka = Role.builder()
                .roleId("ansatt@digit-aggr")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/36")
                .roleName("Ansatt - DIGIT Digitaliseringsavdeling -inkludert underenheter")
                .roleSource("fint")
                .roleType("ansatt")
                .roleStatus("ACTIVE")
                .aggregatedRole(false)
                .memberships(new HashSet<>())
                .build();

        Role roleFromDb = Role.builder()
                .id(3L)
                .roleId("ansatt@digit-aggr")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/36")
                .roleName("Ansatt - DIGIT Digitaliseringsavdeling -inkludert underenheter")
                .roleSource("fint")
                .roleType("ansatt")
                .roleStatus("ACTIVE")
                .aggregatedRole(false)
                .memberships(new HashSet<>())
                .build();

        //given(roleRepository.save(role)).willReturn(role);
        given(roleRepository.findByRoleId("ansatt@digit-aggr")).willReturn(Optional.of(roleFromDb));
        given(roleRepository.save(roleFromDb)).willReturn(roleFromDb);

        // when -  action or the behaviour that we are going test
        Role savedRole = roleService.save(roleFromKafka);
        // then - verify the output

        verify(roleRepository).save(roleFromDb);

        assertThat(savedRole).isEqualTo(roleFromDb);
    }

    @DisplayName("Test for saveRole - status changed date is calculated when status changes")
    @Test
    public void givenExistingRoleWithNewStatus_whenSave_thenUpdateStatusChangedDate() {
        Date oldStatusChanged = Date.from(Instant.parse("2025-01-01T00:00:00Z"));
        Role roleFromKafka = Role.builder()
                .roleId("ansatt@digit-aggr")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/36")
                .roleStatus("INACTIVE")
                .startDate(Date.from(Instant.parse("2026-01-01T00:00:00Z")))
                .endDate(Date.from(Instant.parse("2026-12-31T00:00:00Z")))
                .build();

        Role roleFromDb = Role.builder()
                .id(3L)
                .roleId("ansatt@digit-aggr")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/36")
                .roleStatus("ACTIVE")
                .roleStatusChanged(oldStatusChanged)
                .build();

        given(roleRepository.findByRoleId("ansatt@digit-aggr")).willReturn(Optional.of(roleFromDb));
        given(roleRepository.save(roleFromDb)).willReturn(roleFromDb);

        Role savedRole = roleService.save(roleFromKafka);

        assertThat(savedRole.getRoleStatus()).isEqualTo("INACTIVE");
        assertThat(savedRole.getRoleStatusChanged()).isNotEqualTo(oldStatusChanged);
        assertThat(savedRole.getStartDate()).isEqualTo(roleFromKafka.getStartDate());
        assertThat(savedRole.getEndDate()).isEqualTo(roleFromKafka.getEndDate());
    }

    @DisplayName("Test for saveRole - status changed date is preserved when status is unchanged")
    @Test
    public void givenExistingRoleWithSameStatus_whenSave_thenPreserveStatusChangedDate() {
        Date oldStatusChanged = Date.from(Instant.parse("2025-01-01T00:00:00Z"));
        Role roleFromKafka = Role.builder()
                .roleId("ansatt@digit-aggr")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/36")
                .roleStatus("ACTIVE")
                .startDate(Date.from(Instant.parse("2026-01-01T00:00:00Z")))
                .build();

        Role roleFromDb = Role.builder()
                .id(3L)
                .roleId("ansatt@digit-aggr")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/36")
                .roleStatus("ACTIVE")
                .roleStatusChanged(oldStatusChanged)
                .build();

        given(roleRepository.findByRoleId("ansatt@digit-aggr")).willReturn(Optional.of(roleFromDb));
        given(roleRepository.save(roleFromDb)).willReturn(roleFromDb);

        Role savedRole = roleService.save(roleFromKafka);

        assertThat(savedRole.getRoleStatusChanged()).isEqualTo(oldStatusChanged);
        assertThat(savedRole.getStartDate()).isEqualTo(roleFromKafka.getStartDate());
    }

    @DisplayName("Test for saveRole - save new role")
    @Test
    public void givenRoleObject_whenSaveNewRole_thenReturnNewSavedObject() {

        Role newRole = createNewRole();

        given(roleRepository.findByRoleId("ansatt@digit-fagtj")).willReturn(Optional.empty());
        given(roleRepository.save(newRole)).willReturn(newRole);

        Role savedRole = roleService.save(newRole);

        verify(roleRepository).save(newRole);

        assertThat(savedRole).isEqualTo(newRole);
        assertThat(savedRole.getMemberships()).isNull();
    }

    @DisplayName("Test for saveRole - save new role with non empty member list")
    @Test
    public void givenRoleObject_whenSaveNewRoleWithMembers_thenReturnNewSavedObjectWithMembersList() {

        Member member1 = Member.builder()
                .id(1L)
                .firstName("Jens")
                .lastName("Nilsen")
                .userType("EMPLOYEE")
                .build();

        Member member2 = Member.builder()
                .id(2L)
                .firstName("Anne")
                .lastName("Jensen")
                .userType("EMPLOYEE")
                .build();

        List<Member> members = new ArrayList<>();
        members.add(member1);
        members.add(member2);

        Role newRole = createNewRole();

        MembershipId membershipId1 = new MembershipId(newRole.getId(), member1.getId());
        Membership membership1 = Membership.builder()
                .id(membershipId1)
                .membershipStatus("ACTIVE")
                .build();

        MembershipId membershipId2 = new MembershipId(newRole.getId(), member2.getId());
        Membership membership2 = Membership.builder()
                .id(membershipId2)
                .membershipStatus("ACTIVE")
                .build();

        List<Membership> memberships = new ArrayList<>();
        memberships.add(membership1);
        memberships.add(membership2);

        newRole.setMemberships(new HashSet<>(memberships));

        given(roleRepository.findByRoleId("ansatt@digit-fagtj")).willReturn(Optional.empty());
        given(roleRepository.save(newRole)).willReturn(newRole);

        Role savedRole = roleService.save(newRole);

        verify(roleRepository).save(newRole);

        assertThat(savedRole).isEqualTo(newRole);
        assertThat(savedRole.getMemberships()).isNotNull();
        assertThat(savedRole.getMemberships().size()).isEqualTo(2);
    }

    @DisplayName("Test for getOrgUnitsInSearch method - no orgunits in filter all orgunits in scope")
    @Test
    public void givenNoOrgUnitsInFilterAndALLORGUNITSInScope_thenReturnALLORGUNITS() {
        List<String> orgUnitsInScope = new ArrayList<>(List.of(OrgUnitType.ALLORGUNITS.name()));

        List<String> returnedOrgUnits = roleService.getOrgUnitsInSearch(null, orgUnitsInScope);

        assertThat(returnedOrgUnits.get(0)).isEqualTo(OrgUnitType.ALLORGUNITS.name());
    }

    @DisplayName("Test for getOrgUnitsInSearch method - subset of orgunits in filter all orgunits in scope")
    @Test
    public void givenScopeOrgUnitsInFilterAndALLORGUNITSInScope_thenReturnOrgUnitsInFilter() {
        List<String> orgUnitsInFilter = new ArrayList<>(List.of("198", "205", "211"));
        List<String> orgUnitsInScope = new ArrayList<>(List.of(OrgUnitType.ALLORGUNITS.name()));

        List<String> returnedOrgUnits = roleService.getOrgUnitsInSearch(orgUnitsInFilter, orgUnitsInScope);

        assertThat(returnedOrgUnits).isEqualTo(orgUnitsInFilter);
    }

    @DisplayName("Test for getOrgUnitsInSearch method - subset of orgunits in filter and scope")
    @Test
    public void givenNonScopeOrgUnitsInFilter_thenReturnOrgUnitsInBothInFilterAndScope() {
        List<String> orgUnitsInFilter = new ArrayList<>(List.of("198", "205", "211", "219"));
        List<String> orgUnitsInScope = new ArrayList<>(List.of("198", "205", "211", "218"));
        List<String> expectedReturnedOrgUnits = new ArrayList<>(List.of("198", "205", "211"));

        List<String> returnedOrgUnits = roleService.getOrgUnitsInSearch(orgUnitsInFilter, orgUnitsInScope);

        assertThat(returnedOrgUnits).isEqualTo(expectedReturnedOrgUnits);
    }

    @DisplayName("Test for getOrgUnitsValidAndInScope method - validOrgUnits is null")
    @Test
    public void givenNullValidOrgUnits_whenGetOrgUnitsValidAndInScope_thenReturnOrgUnitsInScope() {
        List<String> orgUnitsInScope = List.of("198", "205", "211");

        List<String> result = RoleService.getOrgUnitsValidAndInScope(orgUnitsInScope, null);

        assertThat(result).isEqualTo(orgUnitsInScope);
    }

    @DisplayName("Test for getOrgUnitsValidAndInScope method - validOrgUnits is empty")
    @Test
    public void givenEmptyValidOrgUnits_whenGetOrgUnitsValidAndInScope_thenReturnOrgUnitsInScope() {
        List<String> orgUnitsInScope = List.of("198", "205", "211");

        List<String> result = RoleService.getOrgUnitsValidAndInScope(orgUnitsInScope, new ArrayList<>());

        assertThat(result).isEqualTo(orgUnitsInScope);
    }

    @DisplayName("Test for getOrgUnitsValidAndInScope method - orgUnitsInScope contains ALLORGUNITS")
    @Test
    public void givenOrgUnitsInScopeContainsALLORGUNITS_whenGetOrgUnitsValidAndInScope_thenReturnValidOrgUnits() {
        List<String> orgUnitsInScope = List.of("ALLORGUNITS", "198", "205");
        List<String> validOrgUnits = List.of("211", "218");

        List<String> result = RoleService.getOrgUnitsValidAndInScope(orgUnitsInScope, validOrgUnits);

        assertThat(result).isEqualTo(validOrgUnits);
    }

    @DisplayName("Test for getOrgUnitsValidAndInScope method - intersection of orgUnitsInScope and validOrgUnits")
    @Test
    public void givenNonEmptyOrgUnitsInScopeAndValidOrgUnits_whenGetOrgUnitsValidAndInScope_thenReturnIntersection() {
        List<String> orgUnitsInScope = List.of("198", "205", "211", "218");
        List<String> validOrgUnits = List.of("211", "218", "219");

        List<String> result = RoleService.getOrgUnitsValidAndInScope(orgUnitsInScope, validOrgUnits);

        assertThat(result).isEqualTo(List.of("211", "218"));
    }

    @DisplayName("Test for getOrgUnitsValidAndInScope method - no intersection between orgUnitsInScope and validOrgUnits")
    @Test
    public void givenNoIntersectionBetweenOrgUnitsInScopeAndValidOrgUnits_whenGetOrgUnitsValidAndInScope_thenReturnEmptyList() {
        List<String> orgUnitsInScope = List.of("198", "205");
        List<String> validOrgUnits = List.of("211", "218");

        List<String> result = RoleService.getOrgUnitsValidAndInScope(orgUnitsInScope, validOrgUnits);

        assertThat(result).isEqualTo(new ArrayList<>());
    }

    @Test
    void shouldExpireRolesAndTheirMemberships() {
        Member member = Member.builder().id(11L).build();
        Role expiredRole = Role.builder()
                .id(10L)
                .roleId("expired-role")
                .resourceId("http://test.no/expired-role")
                .roleStatus("ACTIVE")
                .endDate(Date.from(Instant.parse("2025-01-01T00:00:00Z")))
                .noOfMembers(1)
                .build();
        Membership membership = Membership.builder()
                .id(new MembershipId(expiredRole.getId(), member.getId()))
                .role(expiredRole)
                .member(member)
                .membershipStatus("ACTIVE")
                .build();
        expiredRole.setMemberships(Set.of(membership));

        given(roleRepository.findExpiredRoles(org.mockito.ArgumentMatchers.any(Date.class))).willReturn(List.of(expiredRole));

        var result = roleService.expireRolesAndMemberships(false);

        assertThat(result.updatedRoles()).isEqualTo(1);
        assertThat(result.updatedMemberships()).isEqualTo(1);
        assertThat(expiredRole.getRoleStatus()).isEqualTo("INACTIVE");
        assertThat(expiredRole.getRoleStatusChanged()).isNotNull();
        assertThat(expiredRole.getNoOfMembers()).isZero();
        assertThat(membership.getMembershipStatus()).isEqualTo("INACTIVE");
        assertThat(membership.getMembershipStatusChanged()).isNotNull();
        verify(roleRepository).save(expiredRole);
        verify(membershipRepository).save(membership);
        verify(roleCatalogPublishingComponent).publishRole(expiredRole);
        verify(roleCatalogMembershipPublishingComponent).publishMembership(membership);
    }
}
