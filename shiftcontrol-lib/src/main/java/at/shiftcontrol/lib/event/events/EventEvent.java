package at.shiftcontrol.lib.event.events;

import at.shiftcontrol.lib.event.RoutingKeys;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.EventPart;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventEvent extends BaseEvent {
    private final EventPart event;

    public EventEvent(String routingKey, EventPart event) {
        super(routingKey);
        this.event = event;
    }

    public static EventEvent of(String routingKey, Event event) {
        return new EventEvent(routingKey, EventPart.of(event));
    }

    public static EventEvent forEventCreated(Event event) {
        return of(RoutingKeys.EVENT_CREATED, event);
    }

    public static EventEvent forEventUpdated(Event event) {
        return of(RoutingKeys.format(RoutingKeys.EVENT_UPDATED,
            Map.of("eventId", String.valueOf(event.getId()))), event);
    }
}
