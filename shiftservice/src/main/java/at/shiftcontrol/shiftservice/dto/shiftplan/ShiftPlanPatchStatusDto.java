package at.shiftcontrol.shiftservice.dto.shiftplan;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.LockStatus;

@Data
@Builder
public class ShiftPlanPatchStatusDto {
    @NotNull
    private LockStatus lockStatus;
}
