package at.shiftcontrol.shiftservice.endpoint;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.user.VolunteerUser;
import at.shiftcontrol.shiftservice.dto.UserProfileDto;
import at.shiftcontrol.shiftservice.service.UserProfileService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserProfileEndpointTest {
    private static final long CURRENT_USER_ID = 123456789;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    @Mock
    private ApplicationUserProvider userProvider;
    @Mock
    private UserProfileService service;
    @InjectMocks
    private UserProfileEndpoint endpoint;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(endpoint).build();
    }

    @Test
    void getCurrentUserProfile_ShouldReturnUserProfileDto() throws Exception {
        // mock the user object
        var currentUser = mock(VolunteerUser.class);
        when(userProvider.getCurrentUser()).thenReturn(currentUser);
        when(currentUser.getUserId()).thenReturn(CURRENT_USER_ID);
        // mock the service
        var userProfileDto = mock(UserProfileDto.class);
        when(service.getUserProfile(CURRENT_USER_ID)).thenReturn(userProfileDto);
        mockMvc
            .perform(MockMvcRequestBuilders
                .get("/api/v1/me/profile")
                .accept(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(userProfileDto)));
        verify(service).getUserProfile(CURRENT_USER_ID);
    }
}

