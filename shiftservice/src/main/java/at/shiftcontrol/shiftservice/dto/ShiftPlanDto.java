package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanDto {
    private String id;
    @NotNull
    private String name;
    private String shortDescription;
    private String longDescription;
}
