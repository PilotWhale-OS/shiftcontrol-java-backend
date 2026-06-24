package at.shiftcontrol.shiftservice.endpoint.internal;

import java.util.List;
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

import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.service.userdirectory.InternalUserDirectoryReadService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InternalUserDirectoryEndpointTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private InternalUserDirectoryReadService service;

    @InjectMocks
    private InternalUserDirectoryEndpoint endpoint;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(endpoint).build();
    }

    @Test
    void getUser_returnsSingleAccountInfo() throws Exception {
        AccountInfoDto account = AccountInfoDto.builder()
            .volunteer(new VolunteerDto("user-1", "Alice", "Anderson"))
            .username("alice")
            .email("alice@example.com")
            .isPlatformAdmin(false)
            .build();
        when(service.getUser("user-1")).thenReturn(account);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/internal/users/user-1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(account)));

        verify(service).getUser("user-1");
    }

    @Test
    void searchUsers_returnsPagedAccounts() throws Exception {
        PaginationDto<AccountInfoDto> page = PaginationDto.<AccountInfoDto>builder()
            .page(0)
            .pages(1)
            .total(1)
            .items(List.of(AccountInfoDto.builder()
                .volunteer(new VolunteerDto("user-1", "Alice", "Anderson"))
                .username("alice")
                .email("alice@example.com")
                .isPlatformAdmin(false)
                .build()))
            .build();
        when(service.searchUsers(0, 10, at.shiftcontrol.shiftservice.dto.user.UserSearchDto.builder().name("ali").build()))
            .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/internal/users")
                .param("page", "0")
                .param("size", "10")
                .param("name", "ali")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(page)));
    }
}
