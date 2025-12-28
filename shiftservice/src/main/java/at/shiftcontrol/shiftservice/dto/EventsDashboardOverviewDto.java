package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

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
