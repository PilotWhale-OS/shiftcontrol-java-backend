package at.shiftcontrol.shiftservice.userdirectory;

import at.shiftcontrol.shiftservice.auth.UserType;

public record DirectoryUser(
    String id,
    String username,
    String firstName,
    String lastName,
    String email,
    String profile,
    UserType userType
) {
}
