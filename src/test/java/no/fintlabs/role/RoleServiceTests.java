package no.fintlabs.role;

import no.fintlabs.cache.FintCache;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
import no.fintlabs.membership.Membership;
import no.fintlabs.membership.MembershipId;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipService;
import no.fintlabs.roleCatalogRole.RoleCatalogRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import no.fintlabs.opa.model.OrgUnitType;
@ExtendWith(MockitoExtension.class)
public class RoleServiceTests {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    FintCache<String, Role> roleCache;
    @Mock
    private  MemberService memberService;
    @Mock
    private RoleCatalogRoleService roleCatalogRoleService;
    @Mock
    private RoleCatalogMembershipService roleCatalogMembershipService;
    @InjectMocks
    private RoleService roleService;
    private Role role, aggrole;
    @BeforeEach
    public void setUp() {
        role = Role.builder()
                .id(1L)
                .roleId("ansatt@digit")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/36")
                .roleName("Ansatt - DIGIT Digitaliseringsavdeling")
                .roleSource("fint")
                .roleType("ansatt")
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
                .aggregatedRole(false)
                .memberships(new HashSet<>())
                .build();

        //given(roleRepository.save(role)).willReturn(role);
        given(roleRepository.findByRoleId("ansatt@digit-aggr")).willReturn(Optional.of(aggrole));
        given(roleRepository.save(roleFromKafka)).willReturn(roleFromDb);
        given(roleCache.containsKey("ansatt@digit-aggr")).willReturn(true);
        //given(roleCache.remove("ansatt@digit-aggr")).willReturn(void);
        // when -  action or the behaviour that we are going test
        String roleId = roleFromKafka.getRoleId();
        Long id = roleRepository.findByRoleId(roleId).get().getId();
        roleFromKafka.setId(id);
        Role savedRole = roleService.save(roleFromKafka);
        // then - verify the output
        assertThat(savedRole).isEqualTo(roleFromDb);
    }

@DisplayName("Test for saveRole - save new role")
@Test
public void givenRoleObject_whenSaveNewRole_thenReturnNewSavedObject() {

    Role newRole = createNewRole(new HashSet<>());

    given(roleRepository.findByRoleId("ansatt@digit-fagtj")).willReturn(Optional.empty());
    given(roleRepository.save(newRole)).willReturn(newRole);
    given(roleCache.containsKey("ansatt@digit-fagtj")).willReturn(true);

    Role savedRole = roleService.save(newRole);

    assertThat(savedRole).isEqualTo(newRole);
    assertThat(savedRole.getMemberships()).isNotNull();
    assertThat(savedRole.getMemberships().size()).isEqualTo(0);
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

        Role newRole = createNewRole(new HashSet<>());

        MembershipId membershipId1 = new MembershipId(newRole.getId(), member1.getId());
        Membership membership1 = Membership.builder()
                .id(membershipId1)
                .isActive(true)
                .build();

        MembershipId membershipId2 = new MembershipId(newRole.getId(), member2.getId());
        Membership membership2 = Membership.builder()
                .id(membershipId2)
                .isActive(true)
                .build();

        List<Membership> memberships = new ArrayList<>();
        memberships.add(membership1);
        memberships.add(membership2);

        newRole.setMemberships(new HashSet<>(memberships));

        given(roleRepository.findByRoleId("ansatt@digit-fagtj")).willReturn(Optional.empty());
        given(roleRepository.save(newRole)).willReturn(newRole);
        given(roleCache.containsKey("ansatt@digit-fagtj")).willReturn(true);

        Role savedRole = roleService.save(newRole);

        assertThat(savedRole).isEqualTo(newRole);
        assertThat(savedRole.getMemberships()).isNotNull();
        assertThat(savedRole.getMemberships().size()).isEqualTo(2);
    }

    private static Role createNewRole(HashSet<Member> members) {
        Role newRole =  Role.builder()
                .id(2L)
                .roleId("ansatt@digit-fagtj")
                .resourceId("https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/47")
                .roleName("Ansatt - DIGIT Fagtjenester")
                .roleSource("fint")
                .roleType("ansatt")
                .aggregatedRole(false)
                //.memberships(members)
                .build();
        return newRole;
    }

    @DisplayName("Test for getOrgUnitsInSearch method - no orgunits in filter all orgunits in scope")
    @Test
    public void givenNoOrgUnitsInFilterAndALLORGUNITSInScope_thenReturnALLORGUNITS() {
        List<String> orgUnitsInScope = new ArrayList<String>(List.of(OrgUnitType.ALLORGUNITS.name()));

        List<String> returnedOrgUnits = roleService.getOrgUnitsInSearch(null, orgUnitsInScope);

        assertThat(returnedOrgUnits.equals(OrgUnitType.ALLORGUNITS.name()));
    }
    @DisplayName("Test for getOrgUnitsInSearch method - subset of orgunits in filter all orgunits in scope")
    @Test
    public void givenScopeOrgUnitsInFilterAndALLORGUNITSInScope_thenReturnOrgUnitsInFilter() {
        List<String> orgUnitsInFilter = new ArrayList<String>(List.of("198", "205", "211"));
        List<String> orgUnitsInScope = new ArrayList<String>(List.of(OrgUnitType.ALLORGUNITS.name()));

        List<String> returnedOrgUnits = roleService.getOrgUnitsInSearch(orgUnitsInFilter, orgUnitsInScope);

        assertThat(returnedOrgUnits.equals(orgUnitsInFilter));
    }

    @DisplayName("Test for getOrgUnitsInSearch method - subset of orgunits in filter and scope")
    @Test
    public void givenNonScopeOrgUnitsInFilter_thenReturnOrgUnitsInBothInFilterAndScope() {
        List<String> orgUnitsInFilter = new ArrayList<String>(List.of("198", "205", "211","219"));
        List<String> orgUnitsInScope = new ArrayList<String>(List.of("198", "205", "211","218"));
        List<String> expectedReturnedOrgUnits = new ArrayList<String>(List.of("198", "205", "211"));

        List<String> returnedOrgUnits = roleService.getOrgUnitsInSearch(orgUnitsInFilter, orgUnitsInScope);

        assertThat(returnedOrgUnits.equals(expectedReturnedOrgUnits));
    }
}
