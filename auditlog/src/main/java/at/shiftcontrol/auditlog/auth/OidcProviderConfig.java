package at.shiftcontrol.auditlog.auth;

import java.util.Set;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class OidcProviderConfig {
    @Bean
    OidcProviderProps oidcProviderProps(Environment environment) {
        Binder binder = Binder.get(environment);

        String issuerUri = firstNonBlank(
            binder.bind("oidc.provider.issuer-uri", String.class).orElse(null),
            buildLegacyIssuerUri(
                binder.bind("keycloak.auth-server-url", String.class).orElse(null),
                binder.bind("keycloak.auth-realm", String.class).orElse(null)
            )
        );
        String jwkSetUri = firstNonBlank(
            binder.bind("oidc.provider.jwk-set-uri", String.class).orElse(null),
            issuerUri == null ? null : appendPath(issuerUri, "protocol/openid-connect/certs")
        );
        Set<String> allowedIssuers = binder.bind("oidc.provider.allowed-issuers", Bindable.setOf(String.class))
            .orElseGet(() -> binder.bind("keycloak.allowed-issuers", Bindable.setOf(String.class))
                .orElse(Set.of()));
        if (allowedIssuers.isEmpty() && issuerUri != null) {
            allowedIssuers = Set.of(issuerUri);
        }

        return new OidcProviderProps(issuerUri, jwkSetUri, allowedIssuers);
    }

    private String buildLegacyIssuerUri(String authServerUrl, String authRealm) {
        if (isBlank(authServerUrl) || isBlank(authRealm)) {
            return null;
        }

        return appendPath(authServerUrl, "realms/" + authRealm.trim());
    }

    private String appendPath(String baseUri, String path) {
        String normalizedBaseUri = baseUri.trim();
        if (!normalizedBaseUri.endsWith("/")) {
            normalizedBaseUri += "/";
        }

        return normalizedBaseUri + path;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }

        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
