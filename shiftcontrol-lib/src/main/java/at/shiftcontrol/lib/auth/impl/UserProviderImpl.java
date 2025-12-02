package at.shiftcontrol.lib.auth.impl;

import at.shiftcontrol.lib.auth.ApplicationUser;
import at.shiftcontrol.lib.auth.UserProvider;
import at.shiftcontrol.lib.exception.UnauthorizedException;
import lombok.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserProviderImpl implements UserProvider {
    @Override
    public @NonNull ApplicationUser getApplicationUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedException("No authentication found");
        }

        var principle = authentication.getPrincipal();
        if (principle == null) {
            throw new UnauthorizedException("No principle found");
        }
        if (!(principle instanceof ApplicationUser)) {
            throw new UnauthorizedException("Principle is not of type ApplicationUser");
        }

        return (ApplicationUser) principle;
    }
}