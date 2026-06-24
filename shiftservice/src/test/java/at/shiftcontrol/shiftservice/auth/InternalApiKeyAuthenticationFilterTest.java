package at.shiftcontrol.shiftservice.auth;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InternalApiKeyAuthenticationFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_authenticatesTrustedServiceWhenApiKeyMatches() throws Exception {
        var filter = new InternalApiKeyAuthenticationFilter("super-secret");
        var request = new MockHttpServletRequest("GET", "/api/internal/users");
        request.addHeader(Authorities.INTERNAL_API_KEY_HEADER, "super-secret");
        var response = new MockHttpServletResponse();

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, (jakarta.servlet.FilterChain) (req, res) -> {});

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
            .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
            .containsExactlyInAnyOrder("ADMIN", Authorities.INTERNAL_USERS_READ);
    }

    @Test
    void doFilterInternal_rejectsInvalidApiKey() throws Exception {
        var filter = new InternalApiKeyAuthenticationFilter("super-secret");
        var request = new MockHttpServletRequest("GET", "/api/internal/users");
        request.addHeader(Authorities.INTERNAL_API_KEY_HEADER, "wrong-secret");
        var response = new MockHttpServletResponse();

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, (jakarta.servlet.FilterChain) (req, res) -> {});

        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_UNAUTHORIZED);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_skipsAuthenticationWhenApiKeyNotConfigured() throws Exception {
        var filter = new InternalApiKeyAuthenticationFilter("");
        var request = new MockHttpServletRequest("GET", "/api/internal/users");
        request.addHeader(Authorities.INTERNAL_API_KEY_HEADER, "super-secret");
        var response = new MockHttpServletResponse();

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, (jakarta.servlet.FilterChain) (req, res) -> {});

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_OK);
    }
}
