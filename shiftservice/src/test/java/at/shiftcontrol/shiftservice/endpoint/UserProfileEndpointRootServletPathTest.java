package at.shiftcontrol.shiftservice.endpoint;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;
import at.shiftcontrol.shiftservice.service.userprofile.NotificationService;
import at.shiftcontrol.shiftservice.service.userprofile.UserProfileService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = UserProfileEndpoint.class,
    properties = "spring.mvc.servlet.path=/",
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "at\\.shiftcontrol\\.shiftservice\\.config\\.TraceIdFilter"
    )
)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserProfileEndpointRootServletPathTest.TestConfig.class)
class UserProfileEndpointRootServletPathTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${spring.mvc.servlet.path}")
    private String servletPath;

    @Test
    void shouldExposeEndpointWithoutAdditionalPrefix() throws Exception {
        Assertions.assertEquals("/", servletPath);

        mockMvc.perform(get("/api/v1/me/profile"))
            .andExpect(status().isOk());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        ApplicationUserProvider applicationUserProvider() {
            return new ApplicationUserProvider() {
                @Override
                public ShiftControlUser getCurrentUser() {
                    return new ShiftControlUser(List.of(), "test-user", "123456789") {
                        @Override
                        public boolean isVolunteerInPlan(long shiftPlanId) {
                            return false;
                        }

                        @Override
                        public boolean isPlannerInPlan(long shiftPlanId) {
                            return false;
                        }

                        @Override
                        public boolean isLockedInPlan(long shiftPlanId) {
                            return false;
                        }
                    };
                }
            };
        }

        @Bean
        @Primary
        UserProfileService userProfileService() {
            return userId -> UserProfileDto.builder()
                .account(null)
                .notifications(List.of())
                .assignedRoles(List.of())
                .planningPlans(List.of())
                .volunteeringPlans(List.of())
                .planningEvents(List.of())
                .volunteeringEvents(List.of())
                .build();
        }

        @Bean
        @Primary
        NotificationService notificationService() {
            return new NotificationService() {
                @Override
                public java.util.Collection<NotificationSettingsDto> getNotificationsForUser(String userId) {
                    return List.of();
                }

                @Override
                public NotificationSettingsDto updateNotificationSetting(String userId, NotificationSettingsDto settingsDto) {
                    return settingsDto;
                }
            };
        }
    }
}
