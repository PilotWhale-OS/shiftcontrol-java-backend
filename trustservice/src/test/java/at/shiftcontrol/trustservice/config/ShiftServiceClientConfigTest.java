package at.shiftcontrol.trustservice.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.trustservice.service.OAuthClientCredentialsTokenService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ShiftServiceClientConfigTest {

    @Test
    void restClient_addsBearerTokenToOutgoingRequests() {
        OAuthClientCredentialsTokenService tokenService = mock(OAuthClientCredentialsTokenService.class);
        when(tokenService.getToken()).thenReturn("token-123");

        ShiftServiceClientConfig config = new ShiftServiceClientConfig();
        RestClient.Builder builder = config.restClientBuilder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://shiftservice.example.test/api/internal/users"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-123"))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        RestClient client = config.restClient(builder, tokenService, "https://shiftservice.example.test/");

        client.post()
            .uri("/api/internal/users")
            .retrieve()
            .toBodilessEntity();

        server.verify();
        assertThat(client).isNotNull();
    }
}
