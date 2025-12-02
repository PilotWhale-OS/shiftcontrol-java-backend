package at.shiftcontrol.lib.auth;

import lombok.NonNull;

public interface UserProvider {
    @NonNull
    ApplicationUser getApplicationUser();
}
