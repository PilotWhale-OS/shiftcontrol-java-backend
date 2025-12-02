package at.shiftcontrol.shiftservice.auth.user;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class ShiftPlannerUser extends VolunteerUser{
    public ShiftPlannerUser(Collection<? extends GrantedAuthority> authorities, String username) {
        super(authorities, username);
    }
}
