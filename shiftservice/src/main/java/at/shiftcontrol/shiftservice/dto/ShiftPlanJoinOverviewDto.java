package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanJoinOverviewDto {
    @NotNull
    private ShiftPlanDto shiftPlanDto;

    @NotNull
    private int attendingVolunteerCount;
}
