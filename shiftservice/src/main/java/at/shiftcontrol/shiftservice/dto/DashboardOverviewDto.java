package at.shiftcontrol.shiftservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.Collections;

@Data
@Builder
public class DashboardOverviewDto {
    @NotNull
    private ShiftPlanDto shiftPlan;
    
    @NotNull
    private EventOverviewDto eventOverview;

    @NotNull
    private OwnShiftPlanStatisticsDto ownShiftPlanStatistics;

    @NotNull
    private OverallShiftPlanStatisticsDto overallShiftPlanStatistics;
    
    private int rewardPoints;
    
    private Collection<ShiftDto> shifts;
    
    private Collection<TradeDto> trades;
    
    private Collection<AuctionDto> auctions;
}
