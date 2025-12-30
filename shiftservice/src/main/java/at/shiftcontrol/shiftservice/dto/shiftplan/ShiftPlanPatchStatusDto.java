package at.shiftcontrol.shiftservice.dto.shiftplan;

import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.LockStatus;

@Data
@Builder
public class ShiftPlanPatchStatusDto {
    private LockStatus lockStatus;
}
