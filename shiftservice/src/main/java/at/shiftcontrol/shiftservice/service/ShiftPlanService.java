package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateRequestDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteCreateResponseDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteDetailsDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanInviteDto;
import at.shiftcontrol.shiftservice.dto.invite.ShiftPlanJoinRequestDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanCreateDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanModificationDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleContentDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleDaySearchDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleFilterValuesDto;
import at.shiftcontrol.shiftservice.dto.shiftplan.ShiftPlanScheduleLayoutDto;
import at.shiftcontrol.shiftservice.type.LockStatus;

public interface ShiftPlanService {
    Collection<ShiftPlanDto> getAll(long eventId);

    ShiftPlanDto get(long shiftPlanId);

    ShiftPlanCreateDto createShiftPlan(long eventId, ShiftPlanModificationDto modificationDto);

    ShiftPlanDto update(long shiftPlanId, ShiftPlanModificationDto modificationDto);

    void delete(long shiftPlanId);

    ShiftPlanScheduleLayoutDto getShiftPlanScheduleLayout(long shiftPlanId, ShiftPlanScheduleFilterDto filterDto);

    ShiftPlanScheduleContentDto getShiftPlanScheduleContent(long shiftPlanId, ShiftPlanScheduleDaySearchDto searchDto)
        ;

    ShiftPlanScheduleFilterValuesDto getShiftPlanScheduleFilterValues(long shiftPlanId);

    ShiftPlanInviteCreateResponseDto createShiftPlanInviteCode(long shiftPlanId, ShiftPlanInviteCreateRequestDto requestDto)
        ;

    void revokeShiftPlanInvite(long inviteId);

    void deleteShiftPlanInvite(long inviteId);

    ShiftPlanInviteDetailsDto getShiftPlanInviteDetails(String inviteCode);

    Collection<ShiftPlanInviteDto> getAllShiftPlanInvites(long shiftPlanId);

    void joinShiftPlan(ShiftPlanJoinRequestDto requestDto);

    void updateLockStatus(long shiftPlanId, LockStatus lockStatus);
}
