package at.shiftcontrol.shiftservice.service.user;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;

public interface UserAdministrationService {
    Collection<UserEventDto> getAllUsers(long page, long size);

    UserEventDto getUser(String userId);

    UserEventDto updateUser(String userId, UserEventUpdateDto updateDto);

    UserEventDto lockUser(String userId, long shiftPlanId);

    UserEventDto unLockuser(String userId, long shiftPlanId);
}
