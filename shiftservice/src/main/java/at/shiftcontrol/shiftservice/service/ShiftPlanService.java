package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleFilterDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.ShiftPlanScheduleLayoutDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinOverviewDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinRequestDto;

public interface ShiftPlanService {
    ShiftPlanScheduleLayoutDto getShiftPlanScheduleLayout(long shiftPlanId, ShiftPlanScheduleFilterDto filterDto) throws NotFoundException, ForbiddenException;

    ShiftPlanScheduleContentDto getShiftPlanScheduleContent(long shiftPlanId, ShiftPlanScheduleDaySearchDto searchDto)
        throws NotFoundException, ForbiddenException;

    ShiftPlanScheduleFilterValuesDto getShiftPlanScheduleFilterValues(long shiftPlanId) throws NotFoundException;

    ShiftPlanInviteCreateResponseDto createShiftPlanInviteCode(long shiftPlanId, ShiftPlanInviteCreateRequestDto requestDto)
        throws NotFoundException, ForbiddenException;

    void revokeShiftPlanInviteCode(String inviteCode) throws NotFoundException, ForbiddenException;

    ShiftPlanInviteDto getShiftPlanInviteDetails(String inviteCode) throws NotFoundException, ForbiddenException;

    Collection<ShiftPlanInviteDto> listShiftPlanInvites(long shiftPlanId) throws NotFoundException, ForbiddenException;

    ShiftPlanJoinOverviewDto joinShiftPlanAsVolunteer(ShiftPlanJoinRequestDto requestDto) throws NotFoundException;
}
