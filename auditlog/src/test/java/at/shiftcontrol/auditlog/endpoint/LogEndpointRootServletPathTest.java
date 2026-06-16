package at.shiftcontrol.auditlog.endpoint;

import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.auditlog.dto.LogEntryDto;
import at.shiftcontrol.auditlog.dto.LogSearchDto;
import at.shiftcontrol.auditlog.service.AuditLogService;
import at.shiftcontrol.lib.dto.PaginationDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = LogEndpoint.class,
    properties = "spring.mvc.servlet.path=/",
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
@AutoConfigureMockMvc(addFilters = false)
@Import(LogEndpointRootServletPathTest.TestConfig.class)
class LogEndpointRootServletPathTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${spring.mvc.servlet.path}")
    private String servletPath;

    @Test
    void shouldExposeEndpointWithoutAdditionalPrefix() throws Exception {
        org.junit.jupiter.api.Assertions.assertEquals("/", servletPath);

        mockMvc.perform(get("/api/v1/log")
                .param("page", "0")
                .param("size", "10")
                .param("startTime", Instant.parse("2026-01-01T00:00:00Z").toString())
                .param("endTime", Instant.parse("2026-01-02T00:00:00Z").toString()))
            .andExpect(status().isOk());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        AuditLogService auditLogService() {
            return new AuditLogService(null) {
                @Override
                public PaginationDto<LogEntryDto> search(int page, int size, LogSearchDto searchDto) {
                    return PaginationDto.<LogEntryDto>builder()
                        .page(page)
                        .pages(0)
                        .total(0)
                        .items(List.of())
                        .build();
                }
            };
        }
    }
}
