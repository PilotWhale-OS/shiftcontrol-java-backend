package at.shiftcontrol.shiftservice.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import at.shiftcontrol.lib.auth.UserAuthenticationToken;
import at.shiftcontrol.shiftservice.auth.user.ServiceUser;

public class InternalApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private static final String INTERNAL_SERVICE_USERNAME = "internal-api-key";
    private static final String INTERNAL_SERVICE_USER_ID = "internal-api-key";

    private final String configuredApiKey;

    public InternalApiKeyAuthenticationFilter(String configuredApiKey) {
        this.configuredApiKey = configuredApiKey == null ? "" : configuredApiKey.trim();
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (configuredApiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String presentedApiKey = request.getHeader(Authorities.INTERNAL_API_KEY_HEADER);
        if (presentedApiKey == null || presentedApiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!apiKeysMatch(presentedApiKey.trim(), configuredApiKey)) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid internal API key");
            return;
        }

        var serviceUser = new ServiceUser(
            List.of(
                new SimpleGrantedAuthority("ADMIN"),
                new SimpleGrantedAuthority(Authorities.INTERNAL_USERS_READ)
            ),
            INTERNAL_SERVICE_USERNAME,
            INTERNAL_SERVICE_USER_ID
        );
        var authentication = new UserAuthenticationToken(
            serviceUser,
            "internal-api-key",
            serviceUser.getAuthorities()
        );
        authentication.setDetails(Authorities.INTERNAL_API_KEY_HEADER);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean apiKeysMatch(String presentedApiKey, String expectedApiKey) {
        return MessageDigest.isEqual(
            presentedApiKey.getBytes(StandardCharsets.UTF_8),
            expectedApiKey.getBytes(StandardCharsets.UTF_8)
        );
    }
}
