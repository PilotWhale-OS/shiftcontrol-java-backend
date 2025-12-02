package at.shiftcontrol.shiftservice.auth.user;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AdminUser extends ShiftPlannerUser{
    public AdminUser(Collection<? extends GrantedAuthority> authorities, String username) {
        super(authorities, username);
    }
}
