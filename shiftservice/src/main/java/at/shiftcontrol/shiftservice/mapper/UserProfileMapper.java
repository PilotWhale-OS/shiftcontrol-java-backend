package at.shiftcontrol.shiftservice.mapper;

import java.util.Set;

import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dto.userprofile.AccountInfoDto;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserProfileMapper {
    public static AccountInfoDto toAccountInfoDto(UserRepresentation user) {
        var userTypeAttr = user.firstAttribute("userType");
        var userType = userTypeAttr == null ? UserType.ASSIGNED : UserType.valueOf(userTypeAttr);
        return AccountInfoDto.builder()
            .userType(userType)
            .id(user.getId())
            .fistName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }

    public static UserProfileDto toUserProfileDto(UserRepresentation user, Set<NotificationSettingsDto> notificationSettings, @NonNull Volunteer volunteer) {
        return UserProfileDto.builder()
            .account(toAccountInfoDto(user))
            .notifications(notificationSettings)
            .assignedRoles(RoleMapper.toRoleDto(volunteer.getRoles()))
            .volunteeringPlans(ConvertUtil.toStringList(volunteer.getVolunteeringPlans().stream().map(ShiftPlan::getId)))
            .planningPlans(ConvertUtil.toStringList(volunteer.getPlanningPlans().stream().map(ShiftPlan::getId)))
            .build();
    }
}
