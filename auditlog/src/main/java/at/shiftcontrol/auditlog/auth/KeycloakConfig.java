package at.shiftcontrol.auditlog.auth;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KeycloakProps.class)
public class KeycloakConfig {}
