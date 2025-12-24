package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.DashboardOverviewDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanJoinRequestDto;

public interface ShiftPlanService {
    DashboardOverviewDto getDashboardOverview(long shiftPlanId) throws NotFoundException, ForbiddenException;

    ShiftPlanScheduleDto getShiftPlanSchedule(long shiftPlanId, ShiftPlanScheduleSearchDto searchDto) throws NotFoundException, ForbiddenException;

    ShiftPlanInviteCreateResponseDto createShiftPlanInviteCode(long shiftPlanId, ShiftPlanInviteCreateRequestDto requestDto)
        throws NotFoundException, ForbiddenException;

    ShiftPlanJoinOverviewDto joinShiftPlanAsVolunteer(ShiftPlanJoinRequestDto requestDto) throws NotFoundException;
}
