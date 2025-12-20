package at.shiftcontrol.shiftservice.dao.userprofile.impl;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.shiftservice.dao.userprofile.UserProfileDao;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;

@Deprecated
@RequiredArgsConstructor
@Component
public class UserProfileDaoImpl implements UserProfileDao {
    @Override
    public Optional<UserProfileDto> findSettingsByUserId(Long userId) {
        return Optional.empty();
    }
}
