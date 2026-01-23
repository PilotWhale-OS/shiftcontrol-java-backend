package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.ShiftPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftEvent extends BaseEvent {
    private final ShiftPart shift;

    public ShiftEvent(String routingKey, ShiftPart shift) {
        super(routingKey);
        this.shift = shift;
    }

    public static ShiftEvent ofInternal(String routingKey, Shift shift) {
        return new ShiftEvent(routingKey, ShiftPart.of(shift));
    }

    public static ShiftEvent shiftCreated(Shift shift) {
        return ofInternal(RoutingKeys.SHIFT_CREATED, shift);
    }

    public static ShiftEvent shiftUpdated(Shift shift) {
        return ofInternal(RoutingKeys.format(RoutingKeys.SHIFT_UPDATED, Map.of("shiftId", String.valueOf(shift.getId()))), shift);
    }

    public static ShiftEvent shiftDeleted(Shift shift) {
        return ofInternal(RoutingKeys.format(RoutingKeys.SHIFT_DELETED, Map.of("shiftId", String.valueOf(shift.getId()))), shift);
    }
}
