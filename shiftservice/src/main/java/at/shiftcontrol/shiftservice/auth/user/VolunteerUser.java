package at.shiftcontrol.shiftservice.auth.user;

import at.shiftcontrol.lib.auth.ApplicationUser;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class VolunteerUser extends ApplicationUser {
    public VolunteerUser(Collection<? extends GrantedAuthority> authorities, String username) {
        super(authorities, username);
    }
}
