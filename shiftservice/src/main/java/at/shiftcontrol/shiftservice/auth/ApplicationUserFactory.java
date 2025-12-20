package at.shiftcontrol.shiftservice.auth;

import java.util.Collections;

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
}
