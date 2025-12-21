package at.shiftcontrol.shiftservice.dto;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardOverviewDto {
    @NotNull
    private ShiftPlanDto shiftPlan;
    @NotNull
    private EventDto eventOverview;

    @NotNull
    private OwnStatisticsDto ownShiftPlanStatistics;
    @NotNull
    private OverallStatisticsDto overallShiftPlanStatistics;
    private int rewardPoints;

    private Collection<ShiftDto> shifts;

    private Collection<TradeDto> trades;
    private Collection<ShiftDto> auctions; // TODO this should be Collection<Assignment>
}
