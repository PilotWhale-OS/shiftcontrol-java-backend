package at.shiftcontrol.auditlog.auth;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public record KeycloakProps(
    String authServerUrl,
    String authRealm,
    Set<String> allowedIssuers
) {}
