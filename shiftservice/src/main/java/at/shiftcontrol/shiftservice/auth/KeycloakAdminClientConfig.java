package at.shiftcontrol.shiftservice.auth;

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

        return Keycloak.getInstance(
            serverUrl,
            "master",
            "admin",
            "password",
            "admin-cli");
    }
}
