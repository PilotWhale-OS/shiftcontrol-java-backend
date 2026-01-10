package at.shiftcontrol.shiftservice.sync.pretalx;

import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.pretalxclient.model.EventList;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.service.EventService;

@Service
public class PretalxSyncService {
    private final PretalxDataGatherer dataGatherer;

    private final EventDao eventDao;
    private final EventService eventService;


    private final String locale;

    public PretalxSyncService(PretalxDataGatherer dataGatherer, EventDao eventDao, EventService eventService, @Value("${pretalx.locale}") String locale) {
        this.dataGatherer = dataGatherer;
        this.eventDao = eventDao;
        this.eventService = eventService;
        this.locale = locale;
    }

    public void syncEvents() {
        var events = dataGatherer.gatherEvents();
    }

    private void syncEvent(EventList ptEvent) {
        if (ptEvent.getName().get(locale) == null) {
            throw new PretalxSyncException("Event name is missing for locale: " + locale + " in event: " + ptEvent.getSlug());
        }
        String eventName = ptEvent.getName().get(locale);
        var startTime = ptEvent.getDateFrom().atStartOfDay(ZoneId.systemDefault()).toInstant();
        var endTime = ptEvent.getDateTo().atStartOfDay(ZoneId.systemDefault()).toInstant();

        var existingEvents = eventService.search(EventSearchDto.builder().name(eventName).build());
        if (existingEvents.isEmpty()) {
            //Create new event
            eventService.createEvent(EventModificationDto.builder()
                .name(eventName)
                .startTime(startTime)
                .endTime(endTime)
                .build());
        } else if (existingEvents.size() == 1) {
            //Update existing event
            var existingEvent = existingEvents.get(0);
            if (existingEvent.getStartTime() != startTime
                || existingEvent.getEndTime() != endTime) {
                eventService.updateEvent(ConvertUtil.idToLong(existingEvent.getId()),
                    EventModificationDto.builder()
                        .name(eventName)
                        .startTime(startTime)
                        .endTime(endTime)
                        .build());
            }
        } else {
            //Todo: Better way to identify events?
            throw new PretalxSyncException("Multiple events found with name: " + eventName);
        }
    }
}
