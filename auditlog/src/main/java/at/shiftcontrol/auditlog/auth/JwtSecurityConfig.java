package at.shiftcontrol.auditlog.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.auth.ApplicationUser;
import at.shiftcontrol.lib.auth.NonDevTestCondition;
import at.shiftcontrol.lib.auth.UserAuthenticationToken;
import at.shiftcontrol.lib.exception.IllegalArgumentException;

@Slf4j
@Conditional(NonDevTestCondition.class)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Configuration
@RequiredArgsConstructor
public class JwtSecurityConfig {
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            var applicationUser = createUser(jwt);
            return new UserAuthenticationToken(applicationUser, jwt.getTokenValue(), applicationUser.getAuthorities());
        };
    }

    @Bean
    public JwtDecoder jwtDecoder(OidcProviderProps oidcProviderProps) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(oidcProviderProps.jwkSetUri()).build();
        decoder.setJwtValidator(token -> {
            String issuer = token.getIssuer() == null ? null : token.getIssuer().toString();
            if (issuer == null || !oidcProviderProps.allowedIssuers().contains(issuer)) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "The iss claim is not valid: " + token.getIssuer(),
                    null));
            }
            return OAuth2TokenValidatorResult.success();
        });
        return decoder;
    }

    private ApplicationUser createUser(Jwt jwt) {
        var username = jwt.getClaimAsString("preferred_username");
        var userId = jwt.getClaimAsString("sub");
        log.debug("Creating ApplicationUser for username {} and userId {}", username, userId);

        if (userId == null) {
            throw new IllegalArgumentException("User token does not contain 'sub' claim");
        }

        var authorities = new ArrayList<GrantedAuthority>();
        Set<String> tokenAuthorities = extractAuthorities(jwt);
        if (tokenAuthorities.stream().anyMatch(this::isPlatformAdminAuthority)) {
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
        }
        return new ApplicationUser(authorities, username) {};
    }

    private Set<String> extractAuthorities(Jwt jwt) {
        LinkedHashSet<String> authorities = new LinkedHashSet<>();
        addClaimValues(authorities, jwt.getClaimAsString("scope"));
        addClaimValues(authorities, jwt.getClaims().get("scp"));
        addClaimValues(authorities, jwt.getClaims().get("roles"));
        addNestedRoleValues(authorities, jwt.getClaims().get("realm_access"));
        addResourceAccessRoles(authorities, jwt.getClaims().get("resource_access"));

        return authorities;
    }

    private void addClaimValues(LinkedHashSet<String> authorities, Object claimValue) {
        if (claimValue instanceof Collection<?> values) {
            values.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .forEach(authorities::add);
            return;
        }

        if (claimValue instanceof String stringValue && !stringValue.isBlank()) {
            Arrays.stream(stringValue.split("\\s+"))
                .filter(value -> !value.isBlank())
                .forEach(authorities::add);
        }
    }

    private void addNestedRoleValues(LinkedHashSet<String> authorities, Object accessClaim) {
        if (accessClaim instanceof Map<?, ?> accessMap) {
            addClaimValues(authorities, accessMap.get("roles"));
        }
    }

    private void addResourceAccessRoles(LinkedHashSet<String> authorities, Object resourceAccessClaim) {
        if (!(resourceAccessClaim instanceof Map<?, ?> resourceAccessMap)) {
            return;
        }

        resourceAccessMap.values().stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .forEach(accessEntry -> addClaimValues(authorities, accessEntry.get("roles")));
    }

    private boolean isPlatformAdminAuthority(String authority) {
        return "ADMIN".equalsIgnoreCase(authority)
            || "admin".equalsIgnoreCase(authority)
            || "shiftcontrol.admin".equals(authority);
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/swagger-ui/**")
                .permitAll()
                .requestMatchers("/v3/api-docs*/**")
                .permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll() // Permit all OPTIONS requests (preflight))
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
