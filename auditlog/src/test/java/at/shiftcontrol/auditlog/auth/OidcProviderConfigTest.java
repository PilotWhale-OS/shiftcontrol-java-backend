package at.shiftcontrol.auditlog.auth;

import java.util.Set;
import org.springframework.mock.env.MockEnvironment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OidcProviderConfigTest {
    private final OidcProviderConfig config = new OidcProviderConfig();

    @Test
    void oidcProviderProps_prefersGenericOidcPropertiesWhenPresent() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("oidc.provider.issuer-uri", "https://id.example.test/realms/audit")
            .withProperty("oidc.provider.jwk-set-uri", "https://id.example.test/custom/jwks")
            .withProperty("oidc.provider.allowed-issuers[0]", "https://id.example.test/realms/audit")
            .withProperty("keycloak.auth-server-url", "https://legacy.example.test/")
            .withProperty("keycloak.auth-realm", "legacy");

        OidcProviderProps props = config.oidcProviderProps(environment);

        assertThat(props.issuerUri()).isEqualTo("https://id.example.test/realms/audit");
        assertThat(props.jwkSetUri()).isEqualTo("https://id.example.test/custom/jwks");
        assertThat(props.allowedIssuers()).containsExactly("https://id.example.test/realms/audit");
    }

    @Test
    void oidcProviderProps_buildsStandardUrisFromLegacyKeycloakSettings() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("keycloak.auth-server-url", "https://legacy.example.test")
            .withProperty("keycloak.auth-realm", "audit")
            .withProperty("keycloak.allowed-issuers[0]", "https://legacy.example.test/realms/audit");

        OidcProviderProps props = config.oidcProviderProps(environment);

        assertThat(props.issuerUri()).isEqualTo("https://legacy.example.test/realms/audit");
        assertThat(props.jwkSetUri()).isEqualTo("https://legacy.example.test/realms/audit/protocol/openid-connect/certs");
        assertThat(props.allowedIssuers()).isEqualTo(Set.of("https://legacy.example.test/realms/audit"));
    }

    @Test
    void oidcProviderProps_defaultsAllowedIssuersToIssuerUri() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("oidc.provider.issuer-uri", "https://id.example.test/realms/audit");

        OidcProviderProps props = config.oidcProviderProps(environment);

        assertThat(props.allowedIssuers()).containsExactly("https://id.example.test/realms/audit");
    }
}
