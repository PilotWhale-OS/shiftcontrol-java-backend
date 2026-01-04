package at.shiftcontrol.shiftservice.auth;

import java.util.Collections;
import java.util.List;

import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.auth.user.AssignedUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ApplicationUserFactory {
    public static ShiftControlUser createUser(UserType userType, String username, String userId, UserAttributeProvider attributeProvider) {
        return switch (userType) {
            case ASSIGNED -> new AssignedUser(Collections.emptyList(), username, userId, attributeProvider);
            case ADMIN -> new AdminUser(List.of(new SimpleGrantedAuthority("ADMIN")), username, userId);
        };
    }
}
