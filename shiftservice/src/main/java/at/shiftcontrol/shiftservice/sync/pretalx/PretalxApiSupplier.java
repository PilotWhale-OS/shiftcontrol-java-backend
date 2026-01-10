package at.shiftcontrol.shiftservice.sync.pretalx;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import org.jspecify.annotations.NonNull;

import at.shiftcontrol.pretalxclient.api.EventsApi;
import at.shiftcontrol.pretalxclient.api.RoomsApi;
import at.shiftcontrol.pretalxclient.api.SlotsApi;
import at.shiftcontrol.pretalxclient.api.SubmissionsApi;
import at.shiftcontrol.pretalxclient.invoker.ApiClient;
import at.shiftcontrol.shiftservice.entity.pretalx.PretalxApiKey;

@Service
public class PretalxApiSupplier {
    private final RestClient.Builder restClientBuilder;
    private final String pretalxHost;

    public PretalxApiSupplier(RestClient.Builder restClientBuilder, @Value("${pretalx.host}") String pretalxHost) {
        this.restClientBuilder = restClientBuilder;
        this.pretalxHost = pretalxHost;
    }

    public EventsApi eventsApi(String apiToken) {
        var restClient = buildRestClient(apiToken);
        var apiClient = new ApiClient(restClient);
        return new EventsApi(apiClient);
    }

    public EventsApi eventsApi(PretalxApiKey apiKey) {
        return eventsApi(apiKey.getApiKey());
    }

    public RoomsApi roomsApi(String apiToken) {
        var restClient = buildRestClient(apiToken);
        var apiClient = new ApiClient(restClient);
        return new RoomsApi(apiClient);
    }

    public RoomsApi roomsApi(PretalxApiKey apiKey) {
        return roomsApi(apiKey.getApiKey());
    }

    public SubmissionsApi submissionsApi(String apiToken) {
        var restClient = buildRestClient(apiToken);
        var apiClient = new ApiClient(restClient);
        return new SubmissionsApi(apiClient);
    }

    public SlotsApi slotsApi(String apiToken) {
        var restClient = buildRestClient(apiToken);
        var apiClient = new ApiClient(restClient);
        return new SlotsApi(apiClient);
    }

    private @NonNull RestClient buildRestClient(String apiToken) {
        return restClientBuilder
            .baseUrl(pretalxHost)
            .defaultHeader("Authorization", "Token " + apiToken)
            .build();
    }
}
