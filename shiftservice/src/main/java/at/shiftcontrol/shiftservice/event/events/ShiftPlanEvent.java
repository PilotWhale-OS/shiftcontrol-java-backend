package at.shiftcontrol.shiftservice.event.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.event.BaseEvent;
import at.shiftcontrol.shiftservice.event.events.parts.ShiftPlanPart;

@Data
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class ShiftPlanEvent extends BaseEvent {
    @JsonIgnore
    private final String routingKey;

    private final ShiftPlanPart shiftPlan;

    public static ShiftPlanEvent of(ShiftPlan shiftPlan, String routingKey) {
        return new ShiftPlanEvent(routingKey, ShiftPlanPart.of(shiftPlan));
    }
}
