package at.shiftcontrol.shiftservice.auth;

import at.shiftcontrol.lib.auth.NonDevTestCondition;
import at.shiftcontrol.lib.auth.UserAuthenticationToken;
import at.shiftcontrol.shiftservice.auth.config.props.KeycloakProps;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Conditional(NonDevTestCondition.class)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Configuration
@RequiredArgsConstructor
public class JwtSecurityConfig {
    private final ApplicationUserManager applicationUserManager;

    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            var applicationUser = applicationUserManager.getOrCreateUser(jwt);
            return new UserAuthenticationToken(applicationUser, jwt.getTokenValue(), applicationUser.getAuthorities());
        };
    }

    @Bean
    public JwtDecoder jwtDecoder(KeycloakProps keycloakProps) {
        String jwkSetUri = keycloakProps.authServerUrl() + "realms/" + keycloakProps.authRealm() + "/protocol/openid-connect/certs";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        decoder.setJwtValidator(token -> {
            if (!keycloakProps.allowedIssuers().contains(token.getIssuer().toString())) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "The iss claim is not valid: " + token.getIssuer(),
                    null));
            }
            return OAuth2TokenValidatorResult.success();
        });
        return decoder;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/swagger-ui/**")
                .permitAll()
                .requestMatchers("/v3/api-docs*/**")
                .permitAll()
                .requestMatchers("/api/v1/reward-points/share/**") // permit access to reward points share endpoint via token
                .permitAll()
                .requestMatchers("/api/v1/invites/{inviteCode}")
                .permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                .permitAll() // Permit all OPTIONS requests (preflight))
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
