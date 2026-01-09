package at.shiftcontrol.shiftservice.sync.pretalx;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.pretalxclient.api.EventsApi;

@Service
@RequiredArgsConstructor
public class PTEventLoader {
    private final EventsApi eventsApi;

    public void getAccessibleEvents() {
        // Implementation to fetch accessible events from PreTalx using eventsApi
        var events = eventsApi.apiEventsList(null, null, null);



    }
}
