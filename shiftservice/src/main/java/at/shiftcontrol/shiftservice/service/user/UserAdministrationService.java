package at.shiftcontrol.shiftservice.service.user;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanUpdateDto;

public interface UserAdministrationService {
    Collection<UserEventDto> getAllUsers(long page, long size);

    Collection<UserPlanDto> getAllPlanUsers(Long shiftPlanId, long page, long size);

    UserEventDto getUser(String userId);

    UserPlanDto getPlanUser(Long shiftPlanId, String userId);

    UserEventDto updateUser(String userId, UserEventUpdateDto updateDto);

    UserPlanDto updatePlanUser(Long shiftPlanId, String userId, UserPlanUpdateDto updateDto);

    UserEventDto lockUser(String userId, long shiftPlanId);

    UserEventDto unLockuser(String userId, long shiftPlanId);
}
