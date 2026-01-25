package at.shiftcontrol.lib.event.events.parts;

import lombok.AllArgsConstructor;
import lombok.Data;

import at.shiftcontrol.lib.entity.Event;

@AllArgsConstructor
@Data
public class EventRefPart {
    private long id;
    private String name;

    public static EventRefPart of(Event event) {
        return new EventRefPart(event.getId(), event.getName());
    }
}
