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
        @Value("${keycloak.client.realm}") String clientRealm,
        @Value("${keycloak.client.id}") String clientId,
        @Value("${keycloak.client.username}") String clientUsername,
        @Value("${keycloak.client.password}") String clientPassword
    ) {

        return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(clientRealm)
            .clientId(clientId)
            .grantType(OAuth2Constants.PASSWORD)
            .username(clientUsername)
            .password(clientPassword)
            .build();
    }
}
