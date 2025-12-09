package at.shiftcontrol.shiftservice.auth.user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;

import at.shiftcontrol.lib.auth.ApplicationUser;

@Getter
public class VolunteerUser extends ApplicationUser {
    private final long userId;

    public VolunteerUser(Collection<? extends GrantedAuthority> authorities, String username, long userId) {
        super(authorities, username);
        this.userId = userId;
    }
}
