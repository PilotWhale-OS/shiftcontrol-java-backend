package at.shiftcontrol.shiftservice.auth;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.keycloak.admin.client.Keycloak;

@Configuration
public class KeycloakAdminClientConfig {
    @Bean
    public Keycloak keycloakAdminClient(
        @Value("${keycloak.auth-server-url}") String serverUrl,
        @Value("${keycloak.realm}") String realm,
        @Value("${keycloak.client-id}") String clientId,
        @Value("${keycloak.client-secret}") String clientSecret
    ) {
        return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(realm)
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(clientId)          // <-- should be "backend"
            .clientSecret(clientSecret)  // <-- your static secret
            .build();

//         return KeycloakBuilder.builder()
//             .serverUrl(serverUrl)
//             .realm(realm)
//             .clientId("admin-cli")
//             .grantType(OAuth2Constants.PASSWORD)
//             .username("cli-admin")
//             .password("admin")
//             .build();
    }
}
