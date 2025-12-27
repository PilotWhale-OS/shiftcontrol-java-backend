package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.event.EventDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShiftPlanDashboardOverviewDto {
    @NotNull
    private ShiftPlanDto shiftPlan;
    @NotNull
    private EventDto eventOverview;

    @NotNull
    private OwnStatisticsDto ownShiftPlanStatistics;
    @NotNull
    private OverallStatisticsDto overallShiftPlanStatistics;
    @NotNull
    private int rewardPoints;

    @NotNull
    private Collection<ShiftDto> shifts;

    @NotNull
    private Collection<TradeDto> trades;
    @NotNull
    private Collection<ShiftDto> auctions; // TODO this should be Collection<Assignment>
}
