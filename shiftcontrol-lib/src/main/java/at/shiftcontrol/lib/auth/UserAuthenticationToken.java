package at.shiftcontrol.lib.auth;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import javax.security.auth.Subject;
import java.util.Collection;

@Getter
public class UserAuthenticationToken extends AbstractAuthenticationToken {
    private final ApplicationUser principal;
    private final String credentials;

    public UserAuthenticationToken(@NonNull ApplicationUser principal,
                                   String credentials,
                                   Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(true);  // must use super, as we override
    }

    @Override
    public boolean implies(Subject subject) {
        return super.implies(subject);
    }
}
