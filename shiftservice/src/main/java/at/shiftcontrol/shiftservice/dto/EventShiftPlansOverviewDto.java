package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

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
