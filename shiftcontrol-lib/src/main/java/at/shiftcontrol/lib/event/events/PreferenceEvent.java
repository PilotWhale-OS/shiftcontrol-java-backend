package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.PositionSlotPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class PreferenceEvent extends BaseEvent {
    private final String volunteerId;
    private final int preferenceLevel;
    private final PositionSlotPart positionSlot;

    public PreferenceEvent(String routingKey, String volunteerId, int preferenceLevel, PositionSlotPart positionSlot) {
        super(routingKey);
        this.volunteerId = volunteerId;
        this.preferenceLevel = preferenceLevel;
        this.positionSlot = positionSlot;
    }

    public static PreferenceEvent ofInternal(String routingKey, String volunteerId, int preferenceLevel, PositionSlot positionSlot) {
        return new PreferenceEvent(routingKey, volunteerId, preferenceLevel, PositionSlotPart.of(positionSlot));
    }

    public static PreferenceEvent preferenceUpdated(String volunteerId, int preferenceLevel, PositionSlot positionSlot) {
        return ofInternal(RoutingKeys.format(RoutingKeys.POSITIONSLOT_PREFERENCE_UPDATED,
            Map.of("positionSlotId", String.valueOf(positionSlot.getId()),
                "volunteerId", volunteerId)), volunteerId, preferenceLevel, positionSlot);
    }
}
