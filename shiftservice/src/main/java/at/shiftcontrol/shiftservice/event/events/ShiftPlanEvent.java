package at.shiftcontrol.shiftservice.event.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.ShiftPlanPart;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftPlanEvent extends BaseEvent {
    private final ShiftPlanPart shiftPlan;

    public ShiftPlanEvent(String routingKey, ShiftPlanPart shiftPlan) {
        super(routingKey);
        this.shiftPlan = shiftPlan;
    }

    public static ShiftPlanEvent of(String routingKey, ShiftPlan shiftPlan) {
        return new ShiftPlanEvent(routingKey, ShiftPlanPart.of(shiftPlan));
    }
}
