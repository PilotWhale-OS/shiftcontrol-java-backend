package at.shiftcontrol.lib.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.ShiftPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftEvent extends BaseEvent {
    private final ShiftPart shift;

    public ShiftEvent(String routingKey, ShiftPart shift) {
        super(routingKey);
        this.shift = shift;
    }

    public static ShiftEvent of(String routingKey, Shift shift) {
        return new ShiftEvent(routingKey, ShiftPart.of(shift));
    }
}
