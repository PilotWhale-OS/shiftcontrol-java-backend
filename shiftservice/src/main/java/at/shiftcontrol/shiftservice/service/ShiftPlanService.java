package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;

public interface ShiftPlanService {
    DashboardOverviewDto getDashboardOverview(long shiftPlanId, long userId) throws NotFoundException;

    ShiftPlanScheduleDto getShiftPlanSchedule(long shiftPlanId, long userId, ShiftPlanScheduleSearchDto searchDto) throws NotFoundException;

    ShiftPlanDto joinShiftPlan(long shiftPlanId, long userId, ShiftPlanJoinRequestDto requestDto);
}
