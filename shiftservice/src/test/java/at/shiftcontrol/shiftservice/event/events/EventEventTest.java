package at.shiftcontrol.shiftservice.event.events;

import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.event.events.parts.EventPart;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class EventEventTest {

    @Test
    void of() {
        Event event = mock(Event.class);
        String routingKey = "routingKey";

        EventPart eventPart = mock(EventPart.class);
        try (var eventPartMock = org.mockito.Mockito.mockStatic(EventPart.class)) {
            eventPartMock.when(() -> EventPart.of(event)).thenReturn(eventPart);

            EventEvent eventEvent = EventEvent.of(event, routingKey);

            assertEquals(eventPart, eventEvent.getEvent());
            assertEquals(routingKey, eventEvent.getRoutingKey());
        }
    }
}

