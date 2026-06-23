package at.shiftcontrol.trustservice.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.trustservice.settings.OAuthClientCredentialsSettings;

@Slf4j
@Service
public class OAuthClientCredentialsTokenService {
    private static final Duration REFRESH_SKEW = Duration.ofSeconds(30);

    private final OAuthClientCredentialsSettings settings;
    private final RestClient restClient;
    private final Clock clock;

    private volatile CachedToken cachedToken;

    @Autowired
    public OAuthClientCredentialsTokenService(
        OAuthClientCredentialsSettings settings,
        RestClient.Builder restClientBuilder
    ) {
        this(settings, restClientBuilder.build(), Clock.systemUTC());
    }

    OAuthClientCredentialsTokenService(
        OAuthClientCredentialsSettings settings,
        RestClient restClient,
        Clock clock
    ) {
        this.settings = settings;
        this.restClient = restClient;
        this.clock = clock;
    }

    public String getToken() {
        CachedToken currentToken = cachedToken;
        Instant now = Instant.now(clock);
        if (currentToken != null && currentToken.isValidAt(now)) {
            return currentToken.value();
        }

        synchronized (this) {
            currentToken = cachedToken;
            now = Instant.now(clock);
            if (currentToken != null && currentToken.isValidAt(now)) {
                return currentToken.value();
            }

            cachedToken = fetchToken(now);
            return cachedToken.value();
        }
    }

    private CachedToken fetchToken(Instant now) {
        var formData = new LinkedMultiValueMap<String, String>();
        formData.add("grant_type", "client_credentials");
        if (settings.getScope() != null && !settings.getScope().isBlank()) {
            formData.add("scope", settings.getScope().trim());
        }

        TokenResponse response = restClient.post()
            .uri(settings.getTokenUrl())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .headers(headers -> headers.setBasicAuth(settings.getClientId(), settings.getClientSecret()))
            .body(formData)
            .retrieve()
            .body(TokenResponse.class);

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new IllegalStateException("OAuth2 token endpoint did not return an access token");
        }

        long expiresIn = response.expiresIn() != null && response.expiresIn() > 0 ? response.expiresIn() : 60L;
        Instant expiresAt = now.plusSeconds(expiresIn);
        log.info("OAuth2 client credentials token received");
        return new CachedToken(response.accessToken(), expiresAt.minus(REFRESH_SKEW));
    }

    private record CachedToken(String value, Instant refreshAfter) {
        private boolean isValidAt(Instant now) {
            return refreshAfter.isAfter(now);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record TokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("expires_in")
        Long expiresIn
    ) {}
}
