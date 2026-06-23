package at.shiftcontrol.shiftservice.endpoint.user;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.shiftservice.service.userdirectory.UserInviteAdministrationService;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserInviteItemEndpointTest {
    private MockMvc mockMvc;

    @Mock
    private UserInviteAdministrationService service;

    @InjectMocks
    private AdminUserInviteItemEndpoint endpoint;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(endpoint).build();
    }

    @Test
    void revokeInvite_delegatesToService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/users/invites/15/revoke"))
            .andExpect(status().isOk());

        verify(service).revokeInvite(15L);
    }
}
