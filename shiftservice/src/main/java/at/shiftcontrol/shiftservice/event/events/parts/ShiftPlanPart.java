package at.shiftcontrol.shiftservice.event.events.parts;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.type.LockStatus;

@AllArgsConstructor
@Data
public class ShiftPlanPart {
    @NotNull
    private String id;
    @NotNull
    private String name;
    private String shortDescription;
    private String longDescription;

    private LockStatus lockStatus;

    public static ShiftPlanPart of(ShiftPlan shiftPlan) {
        return new ShiftPlanPart(
            String.valueOf(shiftPlan.getId()),
            shiftPlan.getName(),
            shiftPlan.getShortDescription(),
            shiftPlan.getLongDescription(),
            shiftPlan.getLockStatus()
        );
    }
}
