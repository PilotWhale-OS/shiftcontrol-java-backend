package at.shiftcontrol.trustservice.service;

import org.springframework.stereotype.Service;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.shiftcontrol.trustservice.settings.KeycloakSettings;

@Service
public class KeycloakService {
    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);
    private String cachedToken;
    private KeycloakSettings settings;

    public KeycloakService(KeycloakSettings settings) {
        this.settings = settings;
    }

    public String getToken() {
        if (cachedToken != null) {
            return cachedToken;
        }
        AccessTokenResponse tokenResponse;
        try (Keycloak keycloak = KeycloakBuilder.builder()
            .serverUrl(settings.getBaseUrl())
            .realm(settings.getRealm())
            .clientId(settings.getClientId())
            .clientSecret(settings.getClientSecret())
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .build()) {
            tokenResponse = keycloak.tokenManager().getAccessToken();
            log.info("keycloak token received");
        }
        cachedToken = tokenResponse.getToken();

        return cachedToken;
    }
}
