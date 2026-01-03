package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanScheduleLayoutDto {
    @NotNull
    @Valid
    private Collection<ScheduleLayoutDto> scheduleLayoutDtos;

    @NotNull
    @Valid
    private ScheduleStatisticsDto scheduleStatistics;
}
