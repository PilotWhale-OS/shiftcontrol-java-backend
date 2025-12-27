package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanInviteDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.invite_join.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleSearchDto;

public interface ShiftPlanService {
    ShiftPlanScheduleDto getShiftPlanSchedule(long shiftPlanId, ShiftPlanScheduleSearchDto searchDto) throws NotFoundException, ForbiddenException;

    ShiftPlanScheduleFilterValuesDto getShiftPlanScheduleFilterValues(long shiftPlanId) throws NotFoundException;

    ShiftPlanInviteCreateResponseDto createShiftPlanInviteCode(long shiftPlanId, ShiftPlanInviteCreateRequestDto requestDto)
        throws NotFoundException, ForbiddenException;

    void revokeShiftPlanInviteCode(String inviteCode) throws NotFoundException, ForbiddenException;

    ShiftPlanInviteDto getShiftPlanInviteDetails(String inviteCode) throws NotFoundException, ForbiddenException;

    Collection<ShiftPlanInviteDto> listShiftPlanInvites(long shiftPlanId) throws NotFoundException, ForbiddenException;

    ShiftPlanJoinOverviewDto joinShiftPlanAsVolunteer(ShiftPlanJoinRequestDto requestDto) throws NotFoundException;
}
