package no.fintlabs.role;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class RoleServiceTests {

    @Mock
    private RoleRepository roleRepository;
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


    @DisplayName("Test for saveRole method")
    @Test
    public void givenRoleObject_whenSaveRole_thenReturnRoleObject() {
        //given(roleRepository.save(role)).willReturn(role);
        given(roleRepository.findByRoleId("ansatt@digit")).willReturn(Optional.of(role));

        System.out.println(roleRepository);
        System.out.println(roleService);

        // when -  action or the behaviour that we are going test
        Role savedRole = roleService.save(role);

        System.out.println(savedRole);
        // then - verify the output
        assertThat(savedRole).isNotNull();
    }

}