package at.shiftcontrol.shiftservice.service.user;

import java.util.Collection;

import jakarta.validation.Valid;

import at.shiftcontrol.shiftservice.dto.PaginationDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventBulkDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanBulkDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanUpdateDto;
import at.shiftcontrol.shiftservice.dto.user.UserSearchDto;

public interface UserAdministrationService {
    PaginationDto<UserEventDto> getAllUsers(int page, int size, UserSearchDto searchDt);

    PaginationDto<UserPlanDto> getAllPlanUsers(Long shiftPlanId, int page, int size);

    UserEventDto getUser(String userId);

    UserPlanDto getPlanUser(Long shiftPlanId, String userId);

    UserEventDto updateUser(String userId, UserEventUpdateDto updateDto);

    UserPlanDto updatePlanUser(Long shiftPlanId, String userId, UserPlanUpdateDto updateDto);

    UserEventDto lockUser(String userId, long shiftPlanId);

    UserEventDto unLockuser(String userId, long shiftPlanId);

    Collection<UserPlanDto> bulkAddRoles(long shiftPlanId, @Valid UserPlanBulkDto updateDto);

    Collection<UserPlanDto> bulkRemoveRoles(long shiftPlanId, @Valid UserPlanBulkDto updateDto);

    Collection<UserEventDto> bulkAddVolunteeringPlans(@Valid UserEventBulkDto updateDto);

    Collection<UserEventDto> bulkRemoveVolunteeringPlans(@Valid UserEventBulkDto updateDto);
}
