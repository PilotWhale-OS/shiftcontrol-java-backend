package at.shiftcontrol.trustservice.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "trust.oauth2.client")
public class OAuthClientCredentialsSettings {
    private String tokenUrl;
    private String clientId;
    private String clientSecret;
    private String scope;
}
