package no.fintlabs.role;

import no.fintlabs.member.Member;
import no.fintlabs.member.MemberResponseFactory;
import no.fintlabs.membership.MembershipRepository;
import no.fintlabs.opa.AuthorizationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
public class RoleControllerTest {

    @MockBean
    private RoleService roleServiceMock;

    @MockBean
    private RoleResponseFactory roleResponseFactory;

    @MockBean
    private MemberResponseFactory memberResponseFactory;

    @MockBean
    private AuthorizationClient authorizationClient;

    @MockBean
    private MembershipRepository membershipRepository;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();

    }

    @Test
    void shouldGetMembersByRoleId() throws Exception {
        List<Member> members = List.of(Member.builder().id(1L).build());

        when(membershipRepository.getMembersByRoleId(anyLong(), anyString(), isA(Pageable.class)))
                .thenReturn(new PageImpl<>(members));

        mockMvc.perform(get("/api/roles/1/members")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.members").isNotEmpty())
                .andExpect(jsonPath("$.members[0].id").value(1L));
    }
}
