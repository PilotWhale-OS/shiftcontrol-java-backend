package at.shiftcontrol.shiftservice.service.user.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.annotation.AdminOnly;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventUpdateDto;
import at.shiftcontrol.shiftservice.service.user.UserAdministrationService;

@Service
@RequiredArgsConstructor
public class UserAdministrationServiceImpl implements UserAdministrationService {
    @Override
    @AdminOnly
    public Collection<UserEventDto> getAllUsersForEvent(long eventId) {
        return List.of();
    }

    @Override
    @AdminOnly
    public UserEventDto getUserForEvent(long eventId, String userId) {
        return null;
    }

    @Override
    @AdminOnly
    public Collection<UserEventDto> updateUserForEvent(long eventId, String userId, UserEventUpdateDto updateDto) {
        return List.of();
    }
}
