package at.shiftcontrol.shiftservice.userdirectory.current;

import at.shiftcontrol.shiftservice.auth.UserType;

public record CurrentSubjectProfile(
    String issuer,
    String subject,
    String preferredUsername,
    String firstName,
    String lastName,
    String email,
    Boolean emailVerified,
    String accessToken,
    UserType userType
) {}
