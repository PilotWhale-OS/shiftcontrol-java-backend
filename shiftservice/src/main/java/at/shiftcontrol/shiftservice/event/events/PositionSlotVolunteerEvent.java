package at.shiftcontrol.shiftservice.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.event.events.parts.PositionSlotPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class PositionSlotVolunteerEvent extends PositionSlotEvent {
    private final String volunteerId;

    public PositionSlotVolunteerEvent(String routingKey, PositionSlotPart positionSlot, String volunteerId) {
        super(routingKey, positionSlot);
        this.volunteerId = volunteerId;
    }

    public static PositionSlotVolunteerEvent of(String routingKey, PositionSlot positionSlot, String volunteerId) {
        return new PositionSlotVolunteerEvent(routingKey, PositionSlotPart.of(positionSlot), volunteerId);
    }
}
