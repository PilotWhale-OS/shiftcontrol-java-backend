package at.shiftcontrol.shiftservice.event.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.EventPart;

@Data
@RequiredArgsConstructor
public class EventEvent extends BaseEvent {
    private final EventPart event;

    @JsonIgnore
    private final String routingKey;

    @Override
    public String getRoutingKey() {
        return routingKey;
    }

    public static EventEvent of(Event event, String routingKey) {
        return new EventEvent(EventPart.of(event), routingKey);
    }
}
