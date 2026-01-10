package at.shiftcontrol.shiftservice.sync.pretalx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import at.shiftcontrol.pretalxclient.api.EventsApi;
import at.shiftcontrol.pretalxclient.invoker.ApiClient;

@Configuration
public class PretalxApiConfiguration {
    private final RestClient restClient;
    private final ApiClient apiClient;

    public PretalxApiConfiguration(RestClient.Builder restClientBuilder, @Value("${pretalxHost}") String preTalxHost) {
        restClient = restClientBuilder.baseUrl(preTalxHost).build();
        apiClient = new ApiClient(restClient);
    }

    @Bean
    public EventsApi eventsApi() {
        return new EventsApi(apiClient);
    }
}
