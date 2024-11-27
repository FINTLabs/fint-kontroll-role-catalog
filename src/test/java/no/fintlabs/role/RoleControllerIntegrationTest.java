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
import no.fintlabs.opa.model.Scope;
import no.fintlabs.roleCatalogMembership.RoleCatalogMembershipEntityProducerService;
import no.fintlabs.roleCatalogRole.RoleCatalogPublishingComponent;
import no.fintlabs.roleCatalogRole.RoleCatalogRoleEntityProducerService;
import no.fintlabs.securityconfig.FintKontrollSecurityConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void shouldGetMembersByRoleId() {
        Role role = createRole();
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
