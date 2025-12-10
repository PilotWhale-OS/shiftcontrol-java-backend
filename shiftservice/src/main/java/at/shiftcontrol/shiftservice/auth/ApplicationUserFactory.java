package at.shiftcontrol.shiftservice.auth;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import at.shiftcontrol.lib.auth.ApplicationUser;
import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftPlannerUser;
import at.shiftcontrol.shiftservice.auth.user.VolunteerUser;

@Component
public class ApplicationUserFactory {
    public ApplicationUser createApplicationUser(Jwt jwt) {
        var roles = jwt.getClaimAsStringList("roles");
        var authorities = filterAndTransformRoles(roles);

        var userTypeString = jwt.getClaimAsString("userType");
        var userType = userTypeString == null ? UserType.VOLUNTEER : UserType.valueOf(userTypeString);

        var username = jwt.getClaimAsString("preferred_username");
        var userIdString = jwt.getClaimAsString("user_id");
        var userId = userIdString == null ? -1L : Long.parseLong(userIdString);

        return switch (userType) {
            case VOLUNTEER -> new VolunteerUser(authorities, username, userId);
            case SHIFT_PLANNER -> new ShiftPlannerUser(authorities, username, userId);
            case ADMIN -> new AdminUser(authorities, username, userId);
        };
    }

    public List<SimpleGrantedAuthority> filterAndTransformRoles(List<String> roles) {
        return roles == null ? Collections.emptyList() : roles.stream()
                .filter(role -> !role.startsWith("ui_"))
                .map(SimpleGrantedAuthority::new).toList();
    }
}
