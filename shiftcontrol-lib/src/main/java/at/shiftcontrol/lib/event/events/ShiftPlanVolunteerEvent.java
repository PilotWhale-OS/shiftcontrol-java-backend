package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.ShiftPlanPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftPlanVolunteerEvent extends ShiftPlanEvent {
    private final String volunteerId;

    public ShiftPlanVolunteerEvent(String routingKey, ShiftPlanPart shiftPlan, String volunteerId) {
        super(routingKey, shiftPlan);
        this.volunteerId = volunteerId;
    }

    public static ShiftPlanVolunteerEvent ofInternal(String routingKey, ShiftPlan shiftPlan, String volunteerId) {
        return new ShiftPlanVolunteerEvent(routingKey, ShiftPlanPart.of(shiftPlan), volunteerId);
    }

    public static ShiftPlanVolunteerEvent joinedAsVolunteer(ShiftPlan shiftPlan, String volunteerId) {
        return ofInternal(RoutingKeys.format(RoutingKeys.SHIFTPLAN_JOINED_VOLUNTEER,
            Map.of("shiftPlanId", String.valueOf(shiftPlan.getId()),
                "volunteerId", volunteerId)), shiftPlan, volunteerId);
    }

    public static ShiftPlanVolunteerEvent joinedAsPlanner(ShiftPlan shiftPlan, String volunteerId) {
        return ofInternal(RoutingKeys.format(RoutingKeys.SHIFTPLAN_JOINED_PLANNER,
            Map.of("shiftPlanId", String.valueOf(shiftPlan.getId()),
                "volunteerId", volunteerId)), shiftPlan, volunteerId);
    }
}
