package at.shiftcontrol.shiftservice.service.user;

import java.util.Collection;

import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;

public interface UserAdministrationService {
    Collection<UserEventDto> getAllUsersForEvent(long eventId);

    UserEventDto getUserForEvent(long eventId, String userId);

    Collection<UserEventDto> updateUserForEvent(long eventId, String userId, UserEventUpdateDto updateDto);
}
