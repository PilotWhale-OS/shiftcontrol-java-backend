package at.shiftcontrol.shiftservice.dto.event;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
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
    private OwnStatisticsDto ownEventStatistics;
    @NotNull
    private OverallStatisticsDto overallEventStatistics;
    @NotNull
    private int rewardPoints;

    @NotNull
    private Collection<ShiftPlanDto> shiftPlans;
}
