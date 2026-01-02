package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.util.Collection;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;

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
    private Collection<AssignmentDto> auctions;
}
