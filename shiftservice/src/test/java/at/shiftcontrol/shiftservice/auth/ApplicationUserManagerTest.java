package at.shiftcontrol.shiftservice.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.auth.user.AssignedUser;
import at.shiftcontrol.shiftservice.auth.user.ServiceUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ApplicationUserManagerTest {
    private ApplicationUserManager applicationUserManager;

    @BeforeEach
    void setUp() {
        applicationUserManager = new ApplicationUserManager(mock(UserAttributeProvider.class));
        applicationUserManager.init();
    }

    @Test
    void getOrCreateUser_mapsAdminRealmRoleToAdminAuthority() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token-value")
            .header("alg", "none")
            .claim("sub", "user-1")
            .claim("preferred_username", "alice")
            .claim("scope", "openid profile")
            .claim("realm_access", java.util.Map.of("roles", java.util.List.of("admin")))
            .build();

        var user = applicationUserManager.getOrCreateUser(jwt);

        assertThat(user).isInstanceOf(AdminUser.class);
        assertThat(user.getAuthorities())
            .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
            .contains("ADMIN", "admin");
    }

    @Test
    void getOrCreateUser_createsServiceUserForMachineTokenScopes() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token-value")
            .header("alg", "none")
            .claim("sub", "service-account-usersync")
            .claim("client_id", "usersync")
            .claim("scope", "shiftservice.users.read")
            .build();

        var user = applicationUserManager.getOrCreateUser(jwt);

        assertThat(user).isInstanceOf(ServiceUser.class);
        assertThat(user.getAuthorities())
            .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
            .containsExactly("shiftservice.users.read");
    }

    @Test
    void getOrCreateUser_rebuildsHumanUserWhenTokenAuthoritiesChange() {
        var assignedJwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token-value-1")
            .header("alg", "none")
            .claim("sub", "user-1")
            .claim("preferred_username", "alice")
            .claim("scope", "openid profile")
            .build();
        var adminJwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token-value-2")
            .header("alg", "none")
            .claim("sub", "user-1")
            .claim("preferred_username", "alice")
            .claim("scope", "openid profile")
            .claim("realm_access", java.util.Map.of("roles", java.util.List.of("admin")))
            .build();

        var assignedUser = applicationUserManager.getOrCreateUser(assignedJwt);
        var adminUser = applicationUserManager.getOrCreateUser(adminJwt);

        assertThat(assignedUser).isInstanceOf(AssignedUser.class);
        assertThat(adminUser).isInstanceOf(AdminUser.class);
    }

    @Test
    void getOrCreateUser_keepsLegacyAdminScopeWorkingDuringTransition() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token-value")
            .header("alg", "none")
            .claim("sub", "user-3")
            .claim("preferred_username", "carol")
            .claim("scope", "openid profile shiftcontrol.admin")
            .build();

        var user = applicationUserManager.getOrCreateUser(jwt);

        assertThat(user).isInstanceOf(AdminUser.class);
        assertThat(user.getAuthorities())
            .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
            .contains("ADMIN", "shiftcontrol.admin");
    }
}
