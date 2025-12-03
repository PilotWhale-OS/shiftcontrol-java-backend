package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventShiftPlansOverviewDto {
    @NotNull
    private EventOverviewDto eventOverview;
    @NotNull
    private OwnShiftPlanStatisticsDto ownShiftPlanStatistics;
    @NotNull
    private OverallShiftPlanStatisticsDto overallShiftPlanStatistics;
    private int rewardPoints;
    private Collection<ShiftPlanDto> shiftPlans;
}
