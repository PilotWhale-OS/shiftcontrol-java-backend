package at.shiftcontrol.shiftservice.auth.user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import lombok.Getter;

import at.shiftcontrol.lib.auth.ApplicationUser;

@Getter
public abstract class ShiftControlUser extends ApplicationUser {
    private final String userId;

    public ShiftControlUser(Collection<? extends GrantedAuthority> authorities, String username, String userId) {
        super(authorities, username);
        this.userId = userId;
    }

    public abstract boolean isVolunteerInShift(long shiftId);

    public abstract boolean isPlannerInShift(long shiftId);
}
