package at.shiftcontrol.lib.event.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.events.parts.PositionSlotPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class PositionSlotEvent extends BaseEvent {
    private final PositionSlotPart positionSlot;

    @JsonCreator
    public PositionSlotEvent(
        @JsonProperty("routingKey") String routingKey,
        @JsonProperty("positionSlot") PositionSlotPart positionSlot) {
        super(routingKey);
        this.positionSlot = positionSlot;
    }

    public static PositionSlotEvent of(String routingKey, PositionSlot positionSlot) {
        return new PositionSlotEvent(routingKey, PositionSlotPart.of(positionSlot));
    }
}
