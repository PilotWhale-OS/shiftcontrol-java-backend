package at.shiftcontrol.shiftservice.sync.pretalx;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.pretalxclient.api.EventsApi;

@RequiredArgsConstructor
@Component
public class PreTalxDataGatherer {
    private final EventsApi eventsApi;


}
