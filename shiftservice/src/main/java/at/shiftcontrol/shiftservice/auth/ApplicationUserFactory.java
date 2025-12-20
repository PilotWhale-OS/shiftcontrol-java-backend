package at.shiftcontrol.shiftservice.auth;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.auth.user.AssignedUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;

public class ApplicationUserFactory {
    public static ShiftControlUser createUser(UserType userType, String username, String userId, UserAttributeProvider attributeProvider) {
        return switch (userType) {
            case ASSIGNED -> new AssignedUser(Collections.emptyList(), username, userId, attributeProvider);
            case ADMIN -> new AdminUser(Collections.emptyList(), username, userId);
        };
    }

    public List<SimpleGrantedAuthority> filterAndTransformRoles(List<String> roles) {
        return roles == null ? Collections.emptyList() : roles.stream()
                .filter(role -> !role.startsWith("ui_"))
                .map(SimpleGrantedAuthority::new).toList();
    }
}
