package at.shiftcontrol.shiftservice.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.PositionSlotPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class PositionSlotEvent extends BaseEvent {
    private final PositionSlotPart positionSlot;

    public PositionSlotEvent(String routingKey, PositionSlotPart positionSlot) {
        super(routingKey);
        this.positionSlot = positionSlot;
    }

    public static PositionSlotEvent of(String routingKey, PositionSlot positionSlot) {
        return new PositionSlotEvent(routingKey, PositionSlotPart.of(positionSlot));
    }
}
