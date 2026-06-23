package at.shiftcontrol.shiftservice.auth;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.auth.user.AssignedUser;
import at.shiftcontrol.shiftservice.auth.user.ServiceUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;

public class ApplicationUserFactory {
    public static ShiftControlUser createHumanUser(
        UserType userType,
        String username,
        String userId,
        Set<String> scopeAuthorities,
        UserAttributeProvider attributeProvider
    ) {
        List<SimpleGrantedAuthority> grantedAuthorities = scopeAuthorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        return switch (userType) {
            case ASSIGNED -> new AssignedUser(grantedAuthorities, username, userId, attributeProvider);
            case ADMIN -> {
                grantedAuthorities.add(new SimpleGrantedAuthority("ADMIN"));
                yield new AdminUser(grantedAuthorities, username, userId);
            }
        };
    }

    public static ShiftControlUser createServiceUser(String username, String userId, Set<String> scopeAuthorities) {
        return new ServiceUser(
            scopeAuthorities.isEmpty()
                ? Collections.emptyList()
                : scopeAuthorities.stream().map(SimpleGrantedAuthority::new).toList(),
            username,
            userId
        );
    }
}
