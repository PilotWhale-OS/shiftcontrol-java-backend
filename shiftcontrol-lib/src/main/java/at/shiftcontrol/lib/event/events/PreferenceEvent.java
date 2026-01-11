package at.shiftcontrol.lib.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.event.BaseEvent;
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

    public static PreferenceEvent of(String routingKey, String volunteerId, int preferenceLevel, PositionSlot positionSlot) {
        return new PreferenceEvent(routingKey, volunteerId, preferenceLevel, PositionSlotPart.of(positionSlot));
    }
}
