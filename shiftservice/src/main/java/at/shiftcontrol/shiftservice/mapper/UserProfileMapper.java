package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Event;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;

@RequiredArgsConstructor
@Service
public class UserProfileMapper {
    public static UserProfileDto toUserProfileDto(DirectoryUser user, Collection<NotificationSettingsDto> notificationSettings, Volunteer volunteer) {
        return UserProfileDto.builder()
            .account(AccountInfoMapper.toDto(user))
            .notifications(notificationSettings)
            .assignedRoles(RoleMapper.toRoleDto(volunteer.getRoles()))
            .volunteeringPlans(ConvertUtil.toStringList(volunteer.getVolunteeringPlans().stream().map(ShiftPlan::getId)))
            .planningPlans(ConvertUtil.toStringList(volunteer.getPlanningPlans().stream().map(ShiftPlan::getId)))
            .volunteeringEvents(ConvertUtil.toStringList(volunteer
                .getVolunteeringPlans().stream()
                .map(ShiftPlan::getEvent)
                .map(Event::getId)
                .distinct()))
            .planningEvents(ConvertUtil.toStringList(volunteer
                .getPlanningPlans().stream()
                .map(ShiftPlan::getEvent)
                .map(Event::getId)
                .distinct()))
            .build();
    }
}
