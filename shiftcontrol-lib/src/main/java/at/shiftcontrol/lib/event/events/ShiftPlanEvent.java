package at.shiftcontrol.lib.event.events;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.event.BaseEvent;
import at.shiftcontrol.lib.event.RoutingKeys;
import at.shiftcontrol.lib.event.events.parts.ShiftPlanPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftPlanEvent extends BaseEvent {
    private final ShiftPlanPart shiftPlan;

    public ShiftPlanEvent(String routingKey, ShiftPlanPart shiftPlan) {
        super(routingKey);
        this.shiftPlan = shiftPlan;
    }

    public static ShiftPlanEvent ofInternal(String routingKey, ShiftPlan shiftPlan) {
        return new ShiftPlanEvent(routingKey, ShiftPlanPart.of(shiftPlan));
    }

    public static ShiftPlanEvent planCreated(ShiftPlan shiftPlan) {
        return ofInternal(RoutingKeys.SHIFTPLAN_CREATED, shiftPlan);
    }

    public static ShiftPlanEvent planUpdated(ShiftPlan shiftPlan) {
        return ofInternal(RoutingKeys.format(RoutingKeys.SHIFTPLAN_UPDATED,
            Map.of("shiftPlanId", String.valueOf(shiftPlan.getId()))), shiftPlan);
    }

    public static ShiftPlanEvent planDeleted(ShiftPlan shiftPlan) {
        return ofInternal(RoutingKeys.format(RoutingKeys.SHIFTPLAN_DELETED,
            Map.of("shiftPlanId", String.valueOf(shiftPlan.getId()))), shiftPlan);
    }

    public static ShiftPlanEvent planLockStatusChanged(ShiftPlan shiftPlan) {
        return ofInternal(RoutingKeys.format(RoutingKeys.SHIFTPLAN_LOCKSTATUS_CHANGED,
            Map.of("shiftPlanId", String.valueOf(shiftPlan.getId()))), shiftPlan);
    }
}
