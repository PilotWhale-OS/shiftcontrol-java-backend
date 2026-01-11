package at.shiftcontrol.trustservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import at.shiftcontrol.trustservice.service.KeycloakService;

@Configuration
public class ShiftServiceClientConfig {
    @Bean
    RestClient restClient(
        KeycloakService keycloakService,
        @Value("${trust.shiftservice.baseUrl}") String baseUrl
    ) {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestInterceptor((request, body, execution) -> {
                String token = keycloakService.getToken();
                request.getHeaders().setBearerAuth(token);
                return execution.execute(request, body);
            })
            .build();
    }
}
