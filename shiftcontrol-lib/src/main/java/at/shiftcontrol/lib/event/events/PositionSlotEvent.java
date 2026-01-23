package at.shiftcontrol.lib.event.events;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
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

    public static PositionSlotEvent ofInternal(String routingKey, PositionSlot positionSlot) {
        return new PositionSlotEvent(routingKey, PositionSlotPart.of(positionSlot));
    }

    public static PositionSlotEvent positionSlotCreated(PositionSlot positionSlot) {
        return ofInternal(RoutingKeys.POSITIONSLOT_CREATED, positionSlot);
    }

    public static PositionSlotEvent positionSlotUpdated(PositionSlot positionSlot) {
        return ofInternal(RoutingKeys.format(RoutingKeys.POSITIONSLOT_UPDATED,
            Map.of("positionSlotId", String.valueOf(positionSlot.getId()))), positionSlot);
    }

    public static PositionSlotEvent positionSlotDeleted(PositionSlot positionSlot) {
        return ofInternal(RoutingKeys.format(RoutingKeys.POSITIONSLOT_DELETED,
            Map.of("positionSlotId", String.valueOf(positionSlot.getId()))), positionSlot);
    }
}
