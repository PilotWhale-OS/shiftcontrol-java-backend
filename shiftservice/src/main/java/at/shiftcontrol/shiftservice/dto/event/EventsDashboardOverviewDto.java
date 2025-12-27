package at.shiftcontrol.shiftservice.dto.event;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDashboardOverviewDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventsDashboardOverviewDto {
    @NotNull
    private Collection<ShiftPlanDashboardOverviewDto> shiftPlanDashboardOverviewDtos;

    @NotNull
    private OwnStatisticsDto ownStatisticsDto;
}
