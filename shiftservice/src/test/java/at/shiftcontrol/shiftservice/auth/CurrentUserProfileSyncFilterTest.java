package at.shiftcontrol.shiftservice.auth;

import java.util.List;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.userdirectory.current.CurrentUserProfileSyncService;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CurrentUserProfileSyncFilterTest {
    @Mock
    private CurrentUserProfileSyncService currentUserProfileSyncService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_syncsAuthenticatedJwtBackedUsers() throws Exception {
        var filter = new CurrentUserProfileSyncFilter(currentUserProfileSyncService);
        var request = new MockHttpServletRequest("GET", "/api/v1/events");
        var response = new MockHttpServletResponse();

        SecurityContextHolder.getContext().setAuthentication(authenticationWithJwtDetails());

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, (jakarta.servlet.FilterChain) (req, res) -> {});

        verify(currentUserProfileSyncService).syncCurrentSubjectIfStale();
    }

    @Test
    void doFilterInternal_skipsRequestsWithoutJwtBackedApplicationUser() throws Exception {
        var filter = new CurrentUserProfileSyncFilter(currentUserProfileSyncService);
        var request = new MockHttpServletRequest("GET", "/api/v1/events");
        var response = new MockHttpServletResponse();

        SecurityContextHolder.getContext().setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "plain-user",
            "secret"
        ));

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, (jakarta.servlet.FilterChain) (req, res) -> {});

        verify(currentUserProfileSyncService, never()).syncCurrentSubjectIfStale();
    }

    @Test
    void doFilterInternal_doesNotBlockRequestWhenSyncFails() throws Exception {
        var filter = new CurrentUserProfileSyncFilter(currentUserProfileSyncService);
        var request = new MockHttpServletRequest("GET", "/api/v1/events");
        var response = new MockHttpServletResponse();

        SecurityContextHolder.getContext().setAuthentication(authenticationWithJwtDetails());
        doThrow(new RuntimeException("boom")).when(currentUserProfileSyncService).syncCurrentSubjectIfStale();

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, (jakarta.servlet.FilterChain) (req, res) -> {});

        verify(currentUserProfileSyncService).syncCurrentSubjectIfStale();
    }

    private org.springframework.security.core.Authentication authenticationWithJwtDetails() {
        ShiftControlUser currentUser = new ShiftControlUser(List.of(), "current-user", "user-1") {
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

        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            currentUser,
            "token-value",
            currentUser.getAuthorities()
        );
        authentication.setDetails(Jwt.withTokenValue("token-value")
            .header("alg", "none")
            .claim("sub", "user-1")
            .build());
        return authentication;
    }
}
