package at.shiftcontrol.shiftservice.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import at.shiftcontrol.shiftservice.auth.config.props.KeycloakProps;

@Configuration
@EnableConfigurationProperties(KeycloakProps.class)
public class KeycloakConfig {}
