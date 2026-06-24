package at.shiftcontrol.shiftservice.userdirectory;

public record DirectoryUser(
    String id,
    String username,
    String firstName,
    String lastName,
    String email,
    String profile,
    boolean isPlatformAdmin
) {
}
