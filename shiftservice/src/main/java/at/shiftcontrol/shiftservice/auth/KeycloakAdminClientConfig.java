package at.shiftcontrol.shiftservice.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import at.shiftcontrol.shiftservice.auth.config.props.KeycloakProps;

@Configuration
public class KeycloakAdminClientConfig {
    @Bean
    public Keycloak keycloakAdminClient(KeycloakProps props) {
        return KeycloakBuilder.builder()
            .serverUrl(props.authServerUrl())
            .realm(props.client().realm())
            .clientId(props.client().id())
            .grantType(OAuth2Constants.PASSWORD)
            .username(props.client().username())
            .password(props.client().password())
            .build();
    }
}
