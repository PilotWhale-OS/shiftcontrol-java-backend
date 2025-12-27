package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleSearchDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinRequestDto;

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
