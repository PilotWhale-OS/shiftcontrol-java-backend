package at.shiftcontrol.shiftservice.dto.shiftplan;

import java.util.Collection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import at.shiftcontrol.shiftservice.dto.AssignmentDto;
import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.dto.event.EventDto;
import at.shiftcontrol.shiftservice.dto.shift.ShiftDto;
import at.shiftcontrol.shiftservice.dto.trade.TradeDto;

@Data
@Builder
public class ShiftPlanDashboardOverviewDto {
    @NotNull
    @Valid
    private ShiftPlanDto shiftPlan;

    @NotNull
    @Valid
    private EventDto eventOverview;

    @NotNull
    @Valid
    private OwnStatisticsDto ownShiftPlanStatistics;

    @NotNull
    @Valid
    private OverallStatisticsDto overallShiftPlanStatistics;

    @NotNull
    @Min(0)
    private int rewardPoints;

    @NotNull
    @Valid
    private Collection<ShiftDto> shifts;

    @NotNull
    @Valid
    private Collection<TradeDto> trades;

    @NotNull
    @Valid
    private Collection<AssignmentDto> auctions;
}
