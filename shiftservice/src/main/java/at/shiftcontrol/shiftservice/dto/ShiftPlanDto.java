package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ShiftPlanDto {
    @NotNull
    private String id;

    @NotNull
    private String name;
    
    private String shortDescription;
    
    private String longDescription;
}
