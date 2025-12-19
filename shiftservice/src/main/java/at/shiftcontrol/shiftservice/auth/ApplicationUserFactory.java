package at.shiftcontrol.shiftservice.auth;

import java.util.Collections;
import java.util.List;

import at.shiftcontrol.shiftservice.auth.user.AssignedUser;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import at.shiftcontrol.lib.auth.ApplicationUser;
import at.shiftcontrol.shiftservice.auth.user.AdminUser;

@Component
public class ApplicationUserFactory {

    public ApplicationUser createApplicationUser(Jwt jwt) {
        var userTypeString = jwt.getClaimAsString("userType");
        var userType = userTypeString == null ? UserType.ASSIGNED : UserType.valueOf(userTypeString);
        System.out.println("Creating user of type: " + userType);

        var username = jwt.getClaimAsString("preferred_username");
        var userIdString = jwt.getClaimAsString("sub");
        if(userIdString == null){
            throw new IllegalArgumentException("User token does not contain 'sub' claim");
        }

        return switch (userType) {
            case ASSIGNED -> new AssignedUser(Collections.emptyList(), username, userIdString);
            case ADMIN -> new AdminUser(Collections.emptyList(), username, userIdString);
        };
    }

    public List<SimpleGrantedAuthority> filterAndTransformRoles(List<String> roles) {
        return roles == null ? Collections.emptyList() : roles.stream()
                .filter(role -> !role.startsWith("ui_"))
                .map(SimpleGrantedAuthority::new).toList();
    }
}
