package at.shiftcontrol.shiftservice.auth;

import org.springframework.stereotype.Component;

import at.shiftcontrol.lib.auth.impl.UserProviderImpl;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;

@Component
public class ApplicationUserProvider extends UserProviderImpl {
    public <T extends ShiftControlUser> T getCurrentUser() {
        return (T) getApplicationUser();
    }

    public boolean currentUserHasAuthority(String authority) {
        return getCurrentUser().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }
}
