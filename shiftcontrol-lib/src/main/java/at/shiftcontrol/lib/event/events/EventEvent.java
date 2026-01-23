package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.EventPart;
import static at.shiftcontrol.lib.event.RoutingKeys.EVENT_CLONED;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventEvent extends BaseEvent {
    private final EventPart event;

    public EventEvent(String routingKey, EventPart event) {
        super(routingKey);
        this.event = event;
    }

    public static EventEvent ofInternal(String routingKey, Event event) {
        return new EventEvent(routingKey, EventPart.of(event));
    }

    public static EventEvent eventCreated(Event event) {
        return ofInternal(RoutingKeys.EVENT_CREATED, event);
    }

    public static EventEvent eventUpdated(Event event) {
        return ofInternal(RoutingKeys.format(RoutingKeys.EVENT_UPDATED,
            Map.of("eventId", String.valueOf(event.getId()))), event);
    }

    public static EventEvent eventDeleted(Event event) {
        return ofInternal(RoutingKeys.format(RoutingKeys.EVENT_DELETED, Map.of("eventId", String.valueOf(event.getId()))), event);
    }

    public static EventEvent eventCloned(Event event, Long originalEventId) {
        return ofInternal(RoutingKeys.format(EVENT_CLONED, Map.of(
                "sourceEventId", String.valueOf(originalEventId),
                "newEventId", String.valueOf(event.getId()))),
            event);
    }

    public static EventEvent eventExported(Event event, String exportFormat) {
        return ofInternal(RoutingKeys.format(RoutingKeys.EVENT_EXPORTED, Map.of(
                "eventId", String.valueOf(event.getId()),
                "exportFormat", exportFormat)),
            event);
    }

    public static EventEvent eventImported(Event event) {
        return ofInternal(RoutingKeys.format(RoutingKeys.EVENT_IMPORTED, Map.of(
            "eventId", String.valueOf(event.getId())
        )), event);
    }
}
