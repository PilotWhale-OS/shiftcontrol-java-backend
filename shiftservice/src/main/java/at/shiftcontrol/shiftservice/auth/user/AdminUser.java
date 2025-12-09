package at.shiftcontrol.shiftservice.auth.user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class AdminUser extends ShiftPlannerUser {
    public AdminUser(Collection<? extends GrantedAuthority> authorities, String username, long userId) {
        super(authorities, username, userId);
    }
}
