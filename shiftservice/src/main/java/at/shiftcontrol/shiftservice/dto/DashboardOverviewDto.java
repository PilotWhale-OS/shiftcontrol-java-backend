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
    @NotNull
    private int rewardPoints;

    @NotNull
    private Collection<ShiftDto> shifts;

    @NotNull
    private Collection<TradeDto> trades;
    @NotNull
    private Collection<ShiftDto> auctions;
}
