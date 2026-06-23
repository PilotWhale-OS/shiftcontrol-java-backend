package at.shiftcontrol.trustservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import at.shiftcontrol.trustservice.service.OAuthClientCredentialsTokenService;

@Configuration
public class ShiftServiceClientConfig {
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient restClient(
        RestClient.Builder restClientBuilder,
        OAuthClientCredentialsTokenService tokenService,
        @Value("${trust.shiftservice.baseUrl}") String baseUrl
    ) {
        return restClientBuilder
            .baseUrl(baseUrl)
            .requestInterceptor((request, body, execution) -> {
                String token = tokenService.getToken();
                request.getHeaders().setBearerAuth(token);
                return execution.execute(request, body);
            })
            .build();
    }
}
