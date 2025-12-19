package at.shiftcontrol.shiftservice.service;

import java.util.List;

import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;
import at.shiftcontrol.shiftservice.dto.ScheduleStatisticsDto;
import at.shiftcontrol.shiftservice.entity.Shift;

public interface StatisticService {
    OwnStatisticsDto getOwnShiftPlanStatistics(List<Shift> userShifts);

    OverallStatisticsDto getOverallShiftPlanStatistics(long shiftPlanId);

    OwnStatisticsDto getOwnEventStatistics(long eventId, long userId);

    OverallStatisticsDto getOverallEventStatistics(long eventId);

    ScheduleStatisticsDto getShiftPlanScheduleStatistics(List<Shift> shifts);
}
