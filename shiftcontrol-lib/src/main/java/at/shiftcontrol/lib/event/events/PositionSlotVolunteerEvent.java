package at.shiftcontrol.lib.event.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.event.events.parts.PositionSlotPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class PositionSlotVolunteerEvent extends PositionSlotEvent {
    private final String volunteerId;

    @JsonCreator
    public PositionSlotVolunteerEvent(
        @JsonProperty("routingKey") String routingKey,
        @JsonProperty("positionSlot") PositionSlotPart positionSlot,
        @JsonProperty("volunteerId") String volunteerId) {
        super(routingKey, positionSlot);
        this.volunteerId = volunteerId;
    }

    public static PositionSlotVolunteerEvent of(String routingKey, PositionSlot positionSlot, String volunteerId) {
        return new PositionSlotVolunteerEvent(routingKey, PositionSlotPart.of(positionSlot), volunteerId);
    }
}
