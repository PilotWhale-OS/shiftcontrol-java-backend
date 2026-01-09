package at.shiftcontrol.lib.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.EventPart;

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
}
