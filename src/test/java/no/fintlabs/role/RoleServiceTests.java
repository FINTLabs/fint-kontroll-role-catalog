package no.fintlabs.role;

import no.fintlabs.member.Member;
import no.fintlabs.member.MemberService;
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
    private  MemberService memberService;
    @Mock
    private RoleCatalogRoleService roleCatalogRoleService;
    @Mock
    private RoleCatalogMembershipService roleCatalogMembershipService;
    @InjectMocks
    private RoleService roleService;
    private Role role;
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
                .members(new HashSet<>())
                .build();
    }
    @DisplayName("Test for saveRole - save existing role")
    @Test
    public void givenRoleObject_whenSaveExistingRole_thenReturnExistingRoleObject() {
        //given(roleRepository.save(role)).willReturn(role);
        given(roleRepository.findByRoleId("ansatt@digit")).willReturn(Optional.of(role));
        // when -  action or the behaviour that we are going test
        Role savedRole = roleService.save(role);
        // then - verify the output
        assertThat(savedRole).isEqualTo(role);
    }

@DisplayName("Test for saveRole - save new role")
@Test
public void givenRoleObject_whenSaveNewRole_thenReturnNewSavedObject() {

    Role newRole = createNewRole(new HashSet<>());

    given(roleRepository.findByRoleId("ansatt@digit-fagtj")).willReturn(Optional.empty());
    given(roleRepository.save(newRole)).willReturn(newRole);

    Role savedRole = roleService.save(newRole);

    assertThat(savedRole).isEqualTo(newRole);
    assertThat(savedRole.getMembers()).isNotNull();
    assertThat(savedRole.getMembers().size()).isEqualTo(0);
}
    @DisplayName("Test for saveRole - save new role with non empty member list")
    @Test
    public void givenRoleObject_whenSaveNewRoleWithNoMembers_thenReturnNewSavedObjectWithEmptyMembersList() {

        Member member = Member.builder()
                .id(1L)
                .firstName("Jens")
                .lastName("Nilsen")
                .userType("EMPLOYEE")
                .build();

        HashSet<Member> members = new HashSet<>();
        members.add(member);

        Role newRole = createNewRole(members);

        given(roleRepository.findByRoleId("ansatt@digit-fagtj")).willReturn(Optional.empty());
        given(roleRepository.save(newRole)).willReturn(newRole);
        given(memberService.save(member)).willReturn(member);

        Role savedRole = roleService.save(newRole);

        assertThat(savedRole).isEqualTo(newRole);
        assertThat(savedRole.getMembers()).isNotNull();
        assertThat(savedRole.getMembers().size()).isEqualTo(1);
        assertThat(savedRole.getMembers().stream().findFirst().get()).isEqualTo(member);
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
                .members(members)
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