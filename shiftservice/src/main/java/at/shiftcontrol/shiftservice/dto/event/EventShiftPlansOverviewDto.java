package at.shiftcontrol.shiftservice.dto.event;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventShiftPlansOverviewDto {
    @NotNull
    @Valid
    private EventDto eventOverview;

    @NotNull
    @Valid
    private OwnStatisticsDto ownEventStatistics;

    @NotNull
    @Valid
    private OverallStatisticsDto overallEventStatistics;

    @NotNull
    @Min(0)
    private int rewardPoints;

    @NotNull
    @Valid
    private Collection<ShiftPlanDto> shiftPlans;
}
