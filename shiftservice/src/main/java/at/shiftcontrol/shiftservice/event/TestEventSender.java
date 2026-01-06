package at.shiftcontrol.shiftservice.event;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.shiftservice.entity.Activity;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.Location;
import at.shiftcontrol.shiftservice.event.events.ActivityEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class TestEventSender {
    private final ApplicationEventPublisher publisher;

    @Scheduled(fixedRate = 10000)
    public void sendTestEvent() {
        Event event = new Event();
        event.setId(1L);
        event.setName("Test Event");

        Location location = new Location();
        location.setId(1L);
        location.setName("Test Location");

        Activity activity = Activity.builder()
                .id(1L)
                .name("Test Activity")
                .description("Test Description")
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(3600))
                .event(event)
                .location(location)
                .readOnly(false)
                .build();

        log.info("Publishing test ActivityCreated event for activity id {}", activity.getId());
        publisher.publishEvent(ActivityEvent.of(RoutingKeys.ACTIVITY_CREATED, activity));
    }
}
