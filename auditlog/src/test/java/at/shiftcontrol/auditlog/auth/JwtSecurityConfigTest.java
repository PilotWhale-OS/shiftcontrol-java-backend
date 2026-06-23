package at.shiftcontrol.auditlog.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtSecurityConfigTest {
    private final JwtSecurityConfig jwtSecurityConfig = new JwtSecurityConfig();

    @Test
    void jwtAuthenticationConverter_mapsAdminRealmRoleToAdminAuthority() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token-value")
            .header("alg", "none")
            .claim("sub", "user-1")
            .claim("preferred_username", "alice")
            .claim("scope", "openid profile")
            .claim("realm_access", java.util.Map.of("roles", java.util.List.of("admin")))
            .build();

        var authentication = jwtSecurityConfig.jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication.getAuthorities())
            .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
            .contains("ADMIN");
    }

    @Test
    void jwtAuthenticationConverter_keepsLegacyAdminScopeWorkingDuringTransition() {
        var jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token-value")
            .header("alg", "none")
            .claim("sub", "user-2")
            .claim("preferred_username", "bob")
            .claim("scope", "openid profile shiftcontrol.admin")
            .build();

        var authentication = jwtSecurityConfig.jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication.getAuthorities())
            .extracting(org.springframework.security.core.GrantedAuthority::getAuthority)
            .contains("ADMIN");
    }
}
