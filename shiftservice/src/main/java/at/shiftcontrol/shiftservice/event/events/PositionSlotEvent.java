package at.shiftcontrol.shiftservice.event.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.PositionSlotPart;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class PositionSlotEvent extends BaseEvent {
    @JsonIgnore
    private final String routingKey;

    private final PositionSlotPart positionSlot;

    public static PositionSlotEvent of(PositionSlot positionSlot, String routingKey) {
        return new PositionSlotEvent(routingKey, PositionSlotPart.of(positionSlot));
    }
}
