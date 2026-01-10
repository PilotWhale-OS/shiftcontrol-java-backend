package at.shiftcontrol.shiftservice.sync.pretalx;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.pretalxclient.model.EventList;
import at.shiftcontrol.pretalxclient.model.Room;
import at.shiftcontrol.pretalxclient.model.Submission;
import at.shiftcontrol.pretalxclient.model.TalkSlot;

@RequiredArgsConstructor
@Component
public class PretalxDataGatherer {
    //Max number of rooms to fetch
    public static final int ROOMS_PAGINATION_LIMIT = 1024;
    public static final int SUBMISSIONS_PAGINATION_LIMIT = 1024;
    public static final int SLOTS_PAGINATION_LIMIT = 1024;

    private final PretalxApiKeyLoader apiKeyLoader;
    private final PretalxApiSupplier apiSupplier;

    public Set<EventList> gatherEvents() {
        var eventLists = new HashSet<EventList>();
        var apiKeys = apiKeyLoader.getActiveApiKeys();
        for (var apiKeyData : apiKeys) {
            var eventsApi = apiSupplier.eventsApi(apiKeyData.getApiKey());
            eventLists.addAll(eventsApi.apiEventsList(null, null, null));
        }

        return eventLists;
    }

    public List<Room> gatherRooms(String eventSlug) {
        var apiKey = apiKeyLoader.getApiKeyForEventSlug(eventSlug);
        var roomsApi = apiSupplier.roomsApi(apiKey);
        return roomsApi.roomsList(eventSlug, ROOMS_PAGINATION_LIMIT, null, 0, null).getResults();
    }

    public List<Submission> gatherSubmissions(String eventSlug) {
        var apiKey = apiKeyLoader.getApiKeyForEventSlug(eventSlug);
        var submissionsApi = apiSupplier.submissionsApi(apiKey);
        return submissionsApi.submissionsList(eventSlug, null, null, null, null, 0, SUBMISSIONS_PAGINATION_LIMIT, null, null, null, null, null).getResults();
    }

    public List<TalkSlot> gatherSlots(String eventSlug) {
        var apiKey = apiKeyLoader.getApiKeyForEventSlug(eventSlug);
        var slotsApi = apiSupplier.slotsApi(apiKey);
        return slotsApi.slotsList(eventSlug, null, null, null, 0, SLOTS_PAGINATION_LIMIT, null, null, null, null, null, null).getResults();
    }
}
