package at.shiftcontrol.shiftservice.service;

import java.util.Collection;

import jakarta.validation.Valid;

import at.shiftcontrol.lib.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventBulkDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanBulkDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanUpdateDto;
import at.shiftcontrol.shiftservice.dto.user.UserSearchDto;

public interface UserAdministrationService {
    PaginationDto<UserEventDto> getAllUsers(int page, int size, UserSearchDto searchDt);

    PaginationDto<UserPlanDto> getAllPlanUsers(Long shiftPlanId, int page, int size, UserSearchDto searchDto);

    UserEventDto getUser(String userId);

    UserEventDto createVolunteer(String userId);

    UserPlanDto getPlanUser(Long shiftPlanId, String userId);

    UserEventDto updateEventUser(String userId, UserEventUpdateDto updateDto);

    UserPlanDto updatePlanUser(Long shiftPlanId, String userId, UserPlanUpdateDto updateDto);

    UserEventDto lockUser(String userId, Collection<Long> shiftPlanId);

    UserEventDto unLockUser(String userId, Collection<Long> shiftPlanId);

    UserEventDto resetUser(String userId, Collection<Long> shiftPlanId);

    Collection<UserPlanDto> bulkAddRoles(long shiftPlanId, @Valid UserPlanBulkDto updateDto);

    Collection<UserPlanDto> bulkRemoveRoles(long shiftPlanId, @Valid UserPlanBulkDto updateDto);

    Collection<UserEventDto> bulkAddVolunteeringPlans(@Valid UserEventBulkDto updateDto);

    Collection<UserEventDto> bulkRemoveVolunteeringPlans(@Valid UserEventBulkDto updateDto);
}
