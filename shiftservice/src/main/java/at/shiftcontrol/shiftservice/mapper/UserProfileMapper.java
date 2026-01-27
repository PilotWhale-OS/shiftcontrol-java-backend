package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import at.shiftcontrol.lib.entity.Event;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.dto.userprofile.NotificationSettingsDto;
import at.shiftcontrol.shiftservice.dto.userprofile.UserProfileDto;

@RequiredArgsConstructor
@Service
public class UserProfileMapper {
    public static UserProfileDto toUserProfileDto(UserRepresentation user, Collection<NotificationSettingsDto> notificationSettings, Volunteer volunteer) {
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
