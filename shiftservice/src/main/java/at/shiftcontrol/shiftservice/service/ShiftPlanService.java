package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;

public interface ShiftPlanService {
    DashboardOverviewDto getDashboardOverview(long shiftPlanId, String userId) throws NotFoundException;

    ShiftPlanScheduleDto getShiftPlanSchedule(long shiftPlanId, String userId, ShiftPlanScheduleSearchDto searchDto) throws NotFoundException;

    ShiftPlanScheduleFilterValuesDto getShiftPlanScheduleFilterValues(long shiftPlanId) throws NotFoundException;

    ShiftPlanJoinOverviewDto joinShiftPlan(long shiftPlanId, String userId, ShiftPlanJoinRequestDto requestDto);
}
