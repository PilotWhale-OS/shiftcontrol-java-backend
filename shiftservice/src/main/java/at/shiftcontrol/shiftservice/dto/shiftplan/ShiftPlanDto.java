package at.shiftcontrol.shiftservice.dto.shiftplan;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.type.LockStatus;

@Data
@Builder
public class ShiftPlanDto {
    @NotNull
    private String id;

    @NotNull
    @Size(max = 50)
    private String name;

    @Size(max = 255)
    private String shortDescription;

    @Size(max = 1024)
    private String longDescription;

    @NotNull
    private LockStatus lockStatus;

    @NotNull
    @Min(0)
    private int defaultNoRolePointsPerMinute;
}
