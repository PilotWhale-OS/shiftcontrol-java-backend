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
public class PreferenceEvent extends BaseEvent {
    @JsonIgnore
    private final String routingKey;

    private final String volunteerId;
    private final int preferenceLevel;
    private final PositionSlotPart positionSlot;

    public static PreferenceEvent of(String routingKey, String volunteerId, int preferenceLevel, PositionSlot positionSlot) {
        return new PreferenceEvent(routingKey, volunteerId, preferenceLevel, PositionSlotPart.of(positionSlot));
    }
}
