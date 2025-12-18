package at.shiftcontrol.shiftservice.dao.UserProfile.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.UserProfile.UserProfileDao;
import at.shiftcontrol.shiftservice.dto.UserProfile.UserProfileDto;
import static java.util.Optional.empty;

@RequiredArgsConstructor
@Component
public class UserProfileDaoImpl implements UserProfileDao {
    @Override
    public Optional<UserProfileDto> findSettingsByUserId(Long userId) {
        return empty();
    }
}
