package at.shiftcontrol.lib.auth;

import java.util.Optional;

import lombok.NonNull;

public interface UserProvider {
    @NonNull
    ApplicationUser getApplicationUser();

    Optional<ApplicationUser> getNullableApplicationUser();
}
