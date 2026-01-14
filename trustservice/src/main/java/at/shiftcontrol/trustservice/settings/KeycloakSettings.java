package at.shiftcontrol.trustservice.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "trust.keycloak")
public class KeycloakSettings {
    private String baseUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
}
