package at.shiftcontrol.shiftservice.dto.assignment;

import at.shiftcontrol.lib.entity.ShiftPlan;

import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentContextDto {

    @Valid
    @NotNull
    AssignmentDto assignment;

    @Valid
    @NotNull
    ShiftContextDto shiftContext;

    @Valid
    @NotNull
    PositionSlotContextDto positionSlotContext;
}
