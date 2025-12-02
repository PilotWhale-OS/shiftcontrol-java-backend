package at.shiftcontrol.shiftservice.auth;

import at.shiftcontrol.lib.auth.ApplicationUser;
import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftPlannerUser;
import at.shiftcontrol.shiftservice.auth.user.VolunteerUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationUserFactory {
    public ApplicationUser createApplicationUser(Jwt jwt) {
        var roles = jwt.getClaimAsStringList("roles");
        var authorities = filterAndTransformRoles(roles);

        var userTypeString = jwt.getClaimAsString("userType");
        var userType = userTypeString == null ? UserType.VOLUNTEER : UserType.valueOf(userTypeString);

        var username = jwt.getClaimAsString("preferred_username");

        return switch (userType) {
            case VOLUNTEER -> new VolunteerUser(authorities, username);
            case SHIFT_PLANNER -> new ShiftPlannerUser(authorities, username);
            case ADMIN -> new AdminUser(authorities, username);
        };
    }

    public List<SimpleGrantedAuthority> filterAndTransformRoles(List<String> roles) {
        return roles.stream()
                .filter(role -> !role.startsWith("ui_"))
                .map(role -> role.replaceFirst("sb_", ""))
                .map(SimpleGrantedAuthority::new).toList();
    }
}
