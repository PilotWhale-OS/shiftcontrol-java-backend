package config;

import java.util.List;

import at.shiftcontrol.shiftservice.auth.UserAttributeProvider;
import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.auth.user.AssignedUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class TestAuthFilter extends org.springframework.web.filter.OncePerRequestFilter {

    private final UserAttributeProvider attributeProvider;

    public TestAuthFilter(UserAttributeProvider attributeProvider) {
        this.attributeProvider = attributeProvider;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws java.io.IOException, ServletException {

        String type = request.getHeader(TestSecurityConfig.HDR_TYPE);

        if (type != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = defaultVal(request.getHeader(TestSecurityConfig.HDR_USERID), "test-user");
            String username = defaultVal(request.getHeader(TestSecurityConfig.HDR_USERNAME), "test");

            ShiftControlUser principal;

            if ("ADMIN".equalsIgnoreCase(type)) {
                principal = new AdminUser(
                    List.of(new SimpleGrantedAuthority("ADMIN")),
                    username,
                    userId
                );
            } else {
                principal = new AssignedUser(
                    List.of(),
                    username,
                    userId,
                    attributeProvider
                );
            }

            var auth = new UsernamePasswordAuthenticationToken(
                principal,
                "N/A",
                principal.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static String defaultVal(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }
}
