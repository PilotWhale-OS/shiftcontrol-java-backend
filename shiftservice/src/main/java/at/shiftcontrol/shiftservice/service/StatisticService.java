package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.shiftservice.dto.OverallStatisticsDto;
import at.shiftcontrol.shiftservice.dto.OwnStatisticsDto;

public interface StatisticService {
    OwnStatisticsDto getOwnShiftPlanStatistics(long shiftPlanId, long userId);

    OverallStatisticsDto getOverallShiftPlanStatistics(long shiftPlanId);

    OwnStatisticsDto getOwnEventStatistics(long eventId, long userId);

    OverallStatisticsDto getOverallEventStatistics(long eventId);
}
