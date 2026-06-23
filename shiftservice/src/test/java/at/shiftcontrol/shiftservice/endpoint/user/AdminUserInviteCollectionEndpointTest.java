package at.shiftcontrol.shiftservice.endpoint.user;

import java.time.Instant;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.lib.type.UserInviteStatus;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteCreateDto;
import at.shiftcontrol.shiftservice.dto.userinvite.UserInviteDto;
import at.shiftcontrol.shiftservice.service.userdirectory.UserInviteAdministrationService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserInviteCollectionEndpointTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserInviteAdministrationService service;

    @InjectMocks
    private AdminUserInviteCollectionEndpoint endpoint;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(endpoint).build();
    }

    @Test
    void getAllInvites_returnsPagedInvites() throws Exception {
        PaginationDto<UserInviteDto> page = PaginationDto.<UserInviteDto>builder()
            .page(0)
            .pages(1)
            .total(1)
            .items(List.of(UserInviteDto.builder()
                .id("1")
                .code("INVITE001")
                .email("future@example.com")
                .status(UserInviteStatus.PENDING)
                .createdAt(Instant.parse("2026-06-17T10:00:00Z"))
                .build()))
            .build();
        when(service.getAllInvites(0, 10, at.shiftcontrol.shiftservice.dto.userinvite.UserInviteSearchDto.builder()
            .name("future")
            .status(UserInviteStatus.PENDING)
            .build())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/v1/users/invites")
                .param("page", "0")
                .param("size", "10")
                .param("name", "future")
                .param("status", "PENDING")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(page)));
    }

    @Test
    void createInvite_returnsCreatedInvite() throws Exception {
        UserInviteCreateDto createDto = UserInviteCreateDto.builder()
            .email("future@example.com")
            .build();
        UserInviteDto created = UserInviteDto.builder()
            .id("1")
            .code("INVITE001")
            .email("future@example.com")
            .status(UserInviteStatus.PENDING)
            .createdAt(Instant.parse("2026-06-17T10:00:00Z"))
            .build();
        when(service.createInvite(createDto)).thenReturn(created);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/v1/users/invites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isOk())
            .andExpect(content().string(objectMapper.writeValueAsString(created)));

        verify(service).createInvite(createDto);
    }
}
