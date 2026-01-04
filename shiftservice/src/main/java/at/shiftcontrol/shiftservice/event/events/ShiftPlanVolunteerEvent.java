package at.shiftcontrol.shiftservice.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.event.events.parts.ShiftPlanPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftPlanVolunteerEvent extends ShiftPlanEvent {
    private final String volunteerId;

    public ShiftPlanVolunteerEvent(String routingKey, ShiftPlanPart shiftPlan, String volunteerId) {
        super(routingKey, shiftPlan);
        this.volunteerId = volunteerId;
    }

    public static ShiftPlanVolunteerEvent of(String routingKey, ShiftPlan shiftPlan, String volunteerId) {
        return new ShiftPlanVolunteerEvent(routingKey, ShiftPlanPart.of(shiftPlan), volunteerId);
    }
}
