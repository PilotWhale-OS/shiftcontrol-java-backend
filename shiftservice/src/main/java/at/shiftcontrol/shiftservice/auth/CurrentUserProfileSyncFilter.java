package at.shiftcontrol.shiftservice.auth;

import java.io.IOException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.shiftcontrol.lib.auth.ApplicationUser;
import at.shiftcontrol.shiftservice.userdirectory.current.CurrentUserProfileSyncService;

@RequiredArgsConstructor
public class CurrentUserProfileSyncFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(CurrentUserProfileSyncFilter.class);

    private final CurrentUserProfileSyncService currentUserProfileSyncService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null
                && authentication.getPrincipal() instanceof ApplicationUser
                && authentication.getDetails() instanceof Jwt) {
                currentUserProfileSyncService.syncCurrentSubjectIfStale();
            }
        } catch (RuntimeException exception) {
            log.warn(
                "Failed to sync current user profile for request {} {}. Continuing without blocking the request.",
                request.getMethod(),
                request.getRequestURI(),
                exception
            );
        }

        filterChain.doFilter(request, response);
    }
}
