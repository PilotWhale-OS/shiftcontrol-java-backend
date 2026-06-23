package at.shiftcontrol.trustservice.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.trustservice.settings.OAuthClientCredentialsSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OAuthClientCredentialsTokenServiceTest {

    @Test
    void getToken_reusesCachedTokenUntilRefreshWindow() {
        var builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://idp.example.test/token"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(tokenResponse("token-1", 120), MediaType.APPLICATION_JSON));

        OAuthClientCredentialsTokenService service = new OAuthClientCredentialsTokenService(
            settings(),
            builder.build(),
            Clock.fixed(Instant.parse("2026-06-18T12:00:00Z"), ZoneOffset.UTC)
        );

        assertThat(service.getToken()).isEqualTo("token-1");
        assertThat(service.getToken()).isEqualTo("token-1");

        server.verify();
    }

    @Test
    void getToken_refreshesTokenInsideRefreshWindow() {
        var builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        server.expect(requestTo("https://idp.example.test/token"))
            .andRespond(withSuccess(tokenResponse("token-1", 60), MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://idp.example.test/token"))
            .andRespond(withSuccess(tokenResponse("token-2", 60), MediaType.APPLICATION_JSON));

        MutableClock clock = new MutableClock(Instant.parse("2026-06-18T12:00:00Z"));
        OAuthClientCredentialsTokenService service = new OAuthClientCredentialsTokenService(settings(), builder.build(), clock);

        assertThat(service.getToken()).isEqualTo("token-1");
        clock.setInstant(Instant.parse("2026-06-18T12:00:31Z"));
        assertThat(service.getToken()).isEqualTo("token-2");

        server.verify();
    }

    @Test
    void getToken_sendsClientCredentialsGrantRequest() {
        var builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OAuthClientCredentialsSettings settings = settings();
        settings.setScope("shiftservice.internal");

        server.expect(requestTo("https://idp.example.test/token"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic aW50ZXJuYWw6c3VwZXItc2VjcmV0"))
            .andExpect(content().string("grant_type=client_credentials&scope=shiftservice.internal"))
            .andRespond(withSuccess(tokenResponse("token-1", 120), MediaType.APPLICATION_JSON));

        OAuthClientCredentialsTokenService service = new OAuthClientCredentialsTokenService(
            settings,
            builder.build(),
            Clock.fixed(Instant.parse("2026-06-18T12:00:00Z"), ZoneOffset.UTC)
        );

        assertThat(service.getToken()).isEqualTo("token-1");

        server.verify();
    }

    private OAuthClientCredentialsSettings settings() {
        OAuthClientCredentialsSettings settings = new OAuthClientCredentialsSettings();
        settings.setTokenUrl("https://idp.example.test/token");
        settings.setClientId("internal");
        settings.setClientSecret("super-secret");
        return settings;
    }

    private String tokenResponse(String token, int expiresIn) {
        return """
            {
              "access_token": "%s",
              "expires_in": %d,
              "token_type": "Bearer"
            }
            """.formatted(token, expiresIn);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void setInstant(Instant instant) {
            this.instant = instant;
        }
    }
}
