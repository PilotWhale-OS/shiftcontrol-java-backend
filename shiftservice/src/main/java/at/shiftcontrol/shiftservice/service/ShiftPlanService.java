package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;

public interface ShiftPlanService {
    DashboardOverviewDto getDashboardOverview(long shiftPlanId) throws NotFoundException, ForbiddenException;

    ShiftPlanScheduleDto getShiftPlanSchedule(long shiftPlanId, ShiftPlanScheduleSearchDto searchDto) throws NotFoundException, ForbiddenException;

    ShiftPlanJoinOverviewDto joinShiftPlan(long shiftPlanId, ShiftPlanJoinRequestDto requestDto) throws NotFoundException;
}
