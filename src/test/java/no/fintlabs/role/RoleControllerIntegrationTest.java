package no.fintlabs.role;

import no.fintlabs.DatabaseIntegrationTest;
import no.fintlabs.member.Member;
import no.fintlabs.member.MemberConsumer;
import no.fintlabs.member.MemberRepository;
import no.fintlabs.membership.Membership;
import no.fintlabs.membership.MembershipConsumer;
import no.fintlabs.membership.MembershipId;
import no.fintlabs.membership.MembershipRepository;
import no.fintlabs.opa.AuthorizationClient;
import no.fintlabs.opa.model.OrgUnitType;
import no.fintlabs.opa.model.Scope;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipEntityProducerService;
import no.fintlabs.roleCatalogRole.RoleCatalogPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogRoleEntityProducerService;
import no.fintlabs.securityconfig.FintKontrollSecurityConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;


import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Testcontainers
@SpringBootTest
@Import(RoleService.class)
public class RoleControllerIntegrationTest extends DatabaseIntegrationTest {

    @Autowired
    private RoleService roleService;

    @MockBean
    private AuthorizationClient authorizationClient;

    @MockBean
    private RoleCatalogMembershipEntityProducerService roleCatalogMembershipEntityProducerService;

    @MockBean
    private RoleCatalogRoleEntityProducerService roleCatalogRoleEntityProducerService;

    @MockBean
    private RoleCatalogPublishingComponent roleCatalogPublishingComponent;

    @MockBean
    private MembershipConsumer membershipConsumer;

    @MockBean
    private MemberConsumer memberConsumer;

    @MockBean
    private RoleConsumerConfiguration roleConsumerConfiguration;

    @MockBean
    private FintKontrollSecurityConfig fintKontrollSecurityConfig;

    @Autowired
    private RoleController roleController;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private RoleRepository roleRepository;

    Role role, role1, role2, role3, role4;
    RoleDto roleDto, roleDto1, roleDto2, roleDto3, roleDto4;

    @BeforeEach void setUp() {;
        membershipRepository.deleteAll();
        memberRepository.deleteAll();
        roleRepository.deleteAll();
        role = createRole();
        role1 = createRole1();
        role2 = createRole2();
        role3 = createRole3();
        role4 = createRole4();
        roleDto = RoleMapper.toRoleDto(role);
        roleDto1 = RoleMapper.toRoleDto(role1);
        roleDto2 = RoleMapper.toRoleDto(role2);
        roleDto3 = RoleMapper.toRoleDto(role3);
        roleDto4 = RoleMapper.toRoleDto(role4);
    }
    @Test
    @Order(1)
    void shouldGetPagedAndSortedRoles() {
        List<Scope> userScopes = List.of(Scope.builder()
                                                 .objectType("role")
                                                 .orgUnits(List.of(OrgUnitType.ALLORGUNITS.name()))
                                                 .build());

        when(authorizationClient.getUserScopesList()).thenReturn(userScopes);

        Sort sort = Sort.by(Sort.Order.asc("organisationUnitId"), Sort.Order.asc("roleName"));
        Pageable pageable = PageRequest.of(0, 10, sort);

        ResponseEntity<Map<String, Object>> response = roleController.getRolesV1(null, null, null, null, pageable);

        List<RoleDto> foundRoles = (List<RoleDto>) response.getBody().get("roles");
        assertEquals(5, foundRoles.size());

        List<RoleDto> expectedRoles = List.of( roleDto, roleDto2, roleDto1, roleDto3, roleDto4);
        assertEquals(foundRoles, expectedRoles);
   }
    @Test
    @Order(2)
    void whenUserScopeIsLimited_shouldGetRolesInScope() {
        List<Scope> userScopes = List.of(Scope.builder()
                .objectType("role")
                .orgUnits(List.of("V30.30"))
                .build());

        when(authorizationClient.getUserScopesList()).thenReturn(userScopes);

        Sort sort = Sort.by(Sort.Order.asc("organisationUnitId"), Sort.Order.asc("roleName"));
        Pageable pageable = PageRequest.of(0, 10, sort);

        ResponseEntity<Map<String, Object>> response = roleController.getRolesV1(null, null, null, null, pageable);

        List<RoleDto> foundRoles = (List<RoleDto>) response.getBody().get("roles");
        assertEquals(2, foundRoles.size());

        List<RoleDto> expectedRoles = List.of(roleDto1, roleDto3);
        assertEquals(foundRoles, expectedRoles);
    }
    @Test
    @Order(3)
    void whenAggrolesIsTrue_shouldGetAggRole() {
        List<Scope> userScopes = List.of(Scope.builder()
                .objectType("role")
                .orgUnits(List.of(OrgUnitType.ALLORGUNITS.name()))
                .build());

        when(authorizationClient.getUserScopesList()).thenReturn(userScopes);

        Sort sort = Sort.by(Sort.Order.asc("organisationUnitId"), Sort.Order.asc("roleName"));
        Pageable pageable = PageRequest.of(0, 10, sort);

        ResponseEntity<Map<String, Object>> response = roleController.getRolesV1(null, null, null, true, pageable);

        List<RoleDto> foundRoles = (List<RoleDto>) response.getBody().get("roles");
        assertEquals(1, foundRoles.size());

        List<RoleDto> expectedRoles = List.of(roleDto3);
        assertEquals(foundRoles, expectedRoles);
    }
    @Test
    @Order(4)
    void whenMultipleOrgUnitsAndRoleTypesIsSupplied_shouldShouldReturnCorrespondingRoles() {
        List<Scope> userScopes = List.of(Scope.builder()
                .objectType("role")
                .orgUnits(List.of(OrgUnitType.ALLORGUNITS.name()))
                .build());

        when(authorizationClient.getUserScopesList()).thenReturn(userScopes);

        Sort sort = Sort.by(Sort.Order.asc("organisationUnitId"), Sort.Order.asc("roleName"));
        Pageable pageable = PageRequest.of(0, 10, sort);

        ResponseEntity<Map<String, Object>> response = roleController.getRolesV1(null, List.of("V00","V40.10"), List.of("ansatt","elev"), null, pageable);

        List<RoleDto> foundRoles = (List<RoleDto>) response.getBody().get("roles");
        assertEquals(2, foundRoles.size());

        List<RoleDto> expectedRoles = List.of(roleDto, roleDto4);
        assertEquals(foundRoles, expectedRoles);
    }
    @Test
    @Order(5)
    void shouldGetMembersByRoleId() {
        Member member = createMember();
        createMembership(role, member);

        List<Scope> userScopes = List.of(Scope.builder()
                .objectType("role")
                .orgUnits(List.of("ALLORGUNITS"))
                .build());

        when(authorizationClient.getUserScopesList()).thenReturn(userScopes);

        ResponseEntity<Map<String, Object>> roles = roleController.getRoles(null, "%", List.of("ALLORGUNITS"), "ALLTYPES", false, 0, 10);

        assertThat(roles).isNotNull();
        assertThat(roles.getBody()).isNotNull();
        assertThat(roles.getBody().get("roles")).isNotNull();
        List<SimpleRole> foundRoles = (List<SimpleRole>) roles.getBody().get("roles");
        SimpleRole foundRole = foundRoles.get(0);
        assertThat(foundRole.getId()).isEqualTo(role.getId());
        assertThat(foundRole.getMemberships()).isNotNull();
    }

    private void createMembership(Role role, Member member) {
        Membership membership = new Membership();
        membership.setRole(role);
        membership.setMember(member);
        membership.setMembershipStatus("ACTIVE");

        MembershipId membershipId = new MembershipId();
        membershipId.setMemberId(member.getId());
        membershipId.setRoleId(role.getId());
        membership.setId(membershipId);
        membershipRepository.save(membership);
    }

    @NotNull
    private Role createRole() {
        Role role = new Role();
        role.setRoleId("ansatt@varfk");
        role.setResourceId("https://test.test");
        role.setRoleName("Ansatt - VARFK Vår fylkeskommune");
        role.setRoleType("ansatt");
        role.setOrganisationUnitId("V00");
        role.setOrganisationUnitName("VARFK Vår fylkeskommune");
        role.setRoleSource("fint");
        role.setAggregatedRole(false);
        role.setNoOfMembers(1);
        role = roleRepository.save(role);
        return role;
    }
    @NotNull
    private Role createRole1 () {
        Role role = new Role();
        role.setRoleId("ansatt@digit");
        role.setResourceId("https://test.test");
        role.setRoleName("Ansatt - DIGIT Digitaliseringsavdeling");
        role.setRoleType("ansatt");
        role.setOrganisationUnitId("V30.30");
        role.setOrganisationUnitName("DIGIT Digitaliseringsavdeling");
        role.setRoleSource("fint");
        role.setAggregatedRole(false);
        role.setNoOfMembers(1);
        role = roleRepository.save(role);
        return role;
    }
    @NotNull
    private Role createRole2 () {
        Role role = new Role();
        role.setRoleId("ansatt@plan");
        role.setResourceId("https://test.test");
        role.setRoleName("Ansatt - PLAN Samferdsel");
        role.setRoleType("ansatt");
        role.setOrganisationUnitId("V20.10");
        role.setOrganisationUnitName("PLAN Samferdsel");
        role.setRoleSource("fint");
        role.setAggregatedRole(false);
        role.setNoOfMembers(1);
        role = roleRepository.save(role);
        return role;
    }
    @NotNull
    private Role createRole3() {
        Role role = new Role();
        role.setRoleId("ansatt-aggr@digit");
        role.setResourceId("https://test.test");
        role.setRoleName("Ansatt - DIGIT Digitaliseringsavdeling Inkludert underavdelinger");
        role.setRoleType("ansatt");
        role.setOrganisationUnitId("V30.30");
        role.setOrganisationUnitName("DIGIT Digitaliseringsavdeling");
        role.setRoleSource("fint");
        role.setAggregatedRole(true);
        role.setNoOfMembers(1);
        role = roleRepository.save(role);
        return role;
    }
    @NotNull
    private Role createRole4 () {
        Role role = new Role();
        role.setRoleId("elev@gvmidt");
        role.setResourceId("https://test.test");
        role.setRoleName("Elev - VGMIDT Midtbyen videregående skole");
        role.setRoleType("elev");
        role.setOrganisationUnitId("V40.10");
        role.setOrganisationUnitName("VGMIDT Midtbyen videregående skole");
        role.setRoleSource("fint");
        role.setAggregatedRole(false);
        role.setNoOfMembers(1);
        role = roleRepository.save(role);
        return role;
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

}
