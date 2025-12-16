package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventShiftPlansOverviewDto {
    @NotNull
    private EventDto eventOverview;

    @NotNull
    private OwnStatisticsDto ownShiftPlanStatistics;
    @NotNull
    private OverallStatisticsDto overallShiftPlanStatistics;
    private int rewardPoints;

    private Collection<ShiftPlanDto> shiftPlans;
}
