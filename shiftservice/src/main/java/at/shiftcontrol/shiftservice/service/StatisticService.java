package at.shiftcontrol.shiftservice.service;

import java.util.List;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.dto.event.schedule.ScheduleStatisticsDto;

public interface StatisticService {
    OwnStatisticsDto getOwnStatisticsOfShifts(List<Shift> userShifts);

    OverallStatisticsDto getOverallShiftPlanStatistics(ShiftPlan shiftPlan);

    OverallStatisticsDto getOverallEventStatistics(Event event);

    OwnStatisticsDto getOwnStatisticsOfShiftPlans(List<ShiftPlan> shiftPlans, String userId);

    ScheduleStatisticsDto getShiftPlanScheduleStatistics(List<Shift> shifts);
}
