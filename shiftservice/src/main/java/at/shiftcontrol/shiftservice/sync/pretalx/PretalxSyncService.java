package at.shiftcontrol.shiftservice.sync.pretalx;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.pretalxclient.model.EventList;
import at.shiftcontrol.pretalxclient.model.Room;
import at.shiftcontrol.pretalxclient.model.Submission;
import at.shiftcontrol.pretalxclient.model.TalkSlot;
import at.shiftcontrol.shiftservice.dao.ActivityDao;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.LocationDao;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.event.EventModificationDto;
import at.shiftcontrol.shiftservice.dto.event.EventSearchDto;
import at.shiftcontrol.shiftservice.dto.location.LocationSearchDto;
import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.service.EventService;

@Slf4j
@Service
public class PretalxSyncService {
    private final PretalxDataGatherer dataGatherer;

    private final EventService eventService;
    private final EventDao eventDao;
    private final LocationDao locationDao;
    private final ActivityDao activityDao;

    private final String locale;

    public PretalxSyncService(PretalxDataGatherer dataGatherer, EventService eventService, EventDao eventDao, LocationDao locationDao, ActivityDao activityDao, @Value("${pretalx.locale}") String locale) {
        this.dataGatherer = dataGatherer;
        this.eventService = eventService;
        this.eventDao = eventDao;
        this.locationDao = locationDao;
        this.activityDao = activityDao;
        this.locale = locale;
    }

    @Scheduled(cron = "${pretalx.sync.cron}")
    public void syncAll() {
        var events = dataGatherer.gatherEvents();
        log.info("Starting Pretalx synchronization for {} events", events.size());

        for (var ptEvent : events) {
            var eventDto = syncEvent(ptEvent);
            var locationLookuptable = syncRooms(ConvertUtil.idToLong(eventDto.getId()), ptEvent);
            var activities = syncActivities(eventDao.getById(ConvertUtil.idToLong(eventDto.getId())), ptEvent.getSlug(), locationLookuptable);

            log.info("Synchronized event: {} (ID: {}) with {} locations and {} activities",
                eventDto.getName(),
                eventDto.getId(),
                locationLookuptable.size(),
                activities.size());
        }
        log.info("Pretalx synchronization completed. Total events synchronized: {}", events.size());
    }

    private EventDto syncEvent(EventList ptEvent) {
        if (ptEvent.getName().get(locale) == null) {
            throw new PretalxSyncException("Event name is missing for locale: " + locale + " in event: " + ptEvent.getSlug());
        }
        String eventName = ptEvent.getName().get(locale);
        var startTime = ptEvent.getDateFrom().atStartOfDay(ZoneId.systemDefault()).toInstant();
        var endTime = ptEvent.getDateTo().atStartOfDay(ZoneId.systemDefault()).toInstant();

        var existingEvents = eventService.search(EventSearchDto.builder().name(eventName).build());
        if (existingEvents.isEmpty()) {
            log.info("Creating new event: {} (Slug: {})", eventName, ptEvent.getSlug());
            //Create new event
            return eventService.createEvent(EventModificationDto.builder()
                .name(eventName)
                .startTime(startTime)
                .endTime(endTime)
                .build());
        } else if (existingEvents.size() == 1) {
            //Update existing event
            var existingEvent = existingEvents.get(0);
            if (existingEvent.getStartTime() != startTime
                || existingEvent.getEndTime() != endTime) {
                log.info("Updating existing event: {} (ID: {})", eventName, existingEvent.getId());
                return eventService.updateEvent(ConvertUtil.idToLong(existingEvent.getId()),
                    EventModificationDto.builder()
                        .name(eventName)
                        .startTime(startTime)
                        .endTime(endTime)
                        .build());
            }
            return existingEvent;
        } else {
            //Todo: Better way to identify events?
            throw new PretalxSyncException("Multiple events found with name: " + eventName);
        }
    }

    private Map<Integer, Location> syncRooms(long eventId, EventList ptEvent) {
        var rooms = dataGatherer.gatherRooms(ptEvent.getSlug());
        var event = eventDao.getById(eventId);

        var locations = new HashMap<Integer, Location>();
        for (var room : rooms) {
            var location = syncRoom(event, room);
            locations.put(room.getId(), location);
        }

        return locations;
    }

    private Location syncRoom(Event event, Room room) {
        var roomName = getStringForLocale(room.getName());
        var locations = locationDao.search(LocationSearchDto.builder().name(roomName).build());

        if (locations.isEmpty()) {
            log.info("Creating new location: {} for event: {} (ID: {})", roomName, event.getName(), event.getId());
            //Create new location
            var location = Location.builder()
                .name(roomName)
                .event(event)
                .readOnly(true)
                .description(room.getDescription() != null ? getStringForLocale(room.getDescription()) : null)
                .build();
            return locationDao.save(location);
        } else if (locations.size() == 1) {
            //Update existing location
            var location = locations.iterator().next();
            if (!location.getName().equals(roomName)
                || (room.getDescription() != null && !getStringForLocale(room.getDescription()).equals(location.getDescription()))) {
                log.info("Updating existing location: {} (ID: {}) for event: {} (ID: {})", roomName, location.getId(), event.getName(), event.getId());
                location.setName(roomName);
                location.setDescription(room.getDescription() != null ? getStringForLocale(room.getDescription()) : null);
                return locationDao.save(location);
            }
            return location;
        } else {
            throw new PretalxSyncException("Multiple locations found with name: " + roomName);
        }
    }

    private List<Activity> syncActivities(Event event, String ptEventSlug, Map<Integer, Location> locationLookupTable) {
        var submissions = dataGatherer.gatherSubmissions(ptEventSlug);
        var slots = dataGatherer.gatherSlots(ptEventSlug);

        var activities = new ArrayList<Activity>(slots.size());
        for (var slot : slots) {
            //Find submission for slot
            var submission = submissions.stream()
                .filter(sub -> sub.getCode().equals(slot.getSubmission()))
                .findFirst().orElseThrow(() -> new PretalxSyncException("No submission found for slot with submission code: " + slot.getSubmission()));

            var activity = syncActivity(event, submission, slot, locationLookupTable);
            activities.add(activity);
        }
        return activities;
    }

    private Activity syncActivity(Event event, Submission submission, TalkSlot slot, Map<Integer, Location> locationLookupTable) {
        var activityOpt = activityDao.findByEventAndName(event.getId(), submission.getTitle());
        var location = locationLookupTable.get(slot.getRoom());
        if (location == null) {
            throw new PretalxSyncException("No location found for room ID: " + slot.getRoom());
        }
        if (slot.getStart() == null || slot.getEnd() == null) {
            throw new PretalxSyncException("Slot start or end time is null for slot with ID: " + slot.getId());
        }

        var startTime = slot.getStart().toInstant();
        var endTime = slot.getEnd().toInstant();

        Activity activity;
        if (activityOpt.isEmpty()) {
            //Create new activity
            activity = Activity.builder()
                .event(event)
                .name(submission.getTitle())
                .description(submission.getDescription())
                .startTime(startTime)
                .endTime(endTime)
                .location(location)
                .readOnly(true)
                .build();
            log.info("Creating new activity: {} for event: {} (ID: {})", activity.getName(), event.getName(), event.getId());

            activity = activityDao.save(activity);
        } else {
            activity = activityOpt.get();
            if (activity.getStartTime() != startTime
                || activity.getEndTime() != endTime
                || activity.getLocation().getId() != location.getId()
                || (submission.getDescription() != null && !submission.getDescription().equals(activity.getDescription()))) {
                //Update existing activity
                activity.setStartTime(startTime);
                activity.setEndTime(endTime);
                activity.setLocation(location);
                activity.setDescription(submission.getDescription());
                activity = activityDao.save(activity);
                log.info("Updating existing activity: {} (ID: {}) for event: {} (ID: {})", activity.getName(), activity.getId(), event.getName(), event.getId());
            }
        }
        return activity;
    }

    private String getStringForLocale(@NonNull Map<String, String> map) {
        return map.get(locale);
    }
}
