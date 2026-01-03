package at.shiftcontrol.shiftservice.dto.shiftplan;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanModificationDto {
    @NotNull
    private String name;

    private String shortDescription;

    private String longDescription;
}
