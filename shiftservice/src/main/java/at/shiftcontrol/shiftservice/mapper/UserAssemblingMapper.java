package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;
import java.util.Collections;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.user.ContactInfoDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanDto;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

@RequiredArgsConstructor
@Service
public class UserAssemblingMapper {
    private final UserDirectoryService userDirectoryService;

    public UserPlanDto toUserPlanDto(Volunteer volunteer, Long planId) {
        var user = userDirectoryService.getUserById(volunteer.getId());
        return UserAssemblingMapper.toUserPlanDto(volunteer, user, planId);
    }

    public static UserPlanDto toUserPlanDto(Volunteer volunteer, DirectoryUser user, Long planId) {
        return UserPlanDto.builder()
            .volunteer(VolunteerAssemblingMapper.toDtoFromUser(user))
            .email(user.email())
            .roles(volunteer == null ? null : RoleMapper.toRoleDto(volunteer.getRoles(), planId))
            .isLocked(volunteer == null ? null : volunteer.getLockedPlans().stream().anyMatch(x -> planId.equals(x.getId())))
            .build();
    }

    public static Collection<UserPlanDto> toUserPlanDto(Collection<Volunteer> volunteers, Collection<DirectoryUser> users, Long planId) {
        return volunteers.stream()
            .map(v ->
                toUserPlanDto(v, users.stream()
                        .filter(u -> u.id().equals(v.getId()))
                        .findFirst()
                        .orElseThrow(NotFoundException::new),
                    planId)
            )
            .sorted(UserPlanDto.lastNameComparator())
            .toList();
    }

    public UserEventDto toUserEventDto(Volunteer volunteer) {
        var user = userDirectoryService.getUserById(volunteer.getId());
        return UserAssemblingMapper.toUserEventDto(volunteer, user);
    }

    public static UserEventDto toUserEventDto(Volunteer volunteer, DirectoryUser user) {
        if (volunteer == null) {
            return UserEventDto.builder()
                .volunteer(VolunteerAssemblingMapper.toDtoFromUser(user))
                .volunteeringPlans(Collections.emptySet())
                .planningPlans(Collections.emptySet())
                .lockedPlans(Collections.emptySet())
                .email(user.email())
                .build();
        }
        return UserEventDto.builder()
            .volunteer(VolunteerAssemblingMapper.toDtoFromUser(user))
            .email(user.email())
            .volunteeringPlans(volunteer.getVolunteeringPlans()
                .stream()
                .map(shiftPlan -> String.valueOf(shiftPlan.getId()))
                .toList())
            .planningPlans(volunteer.getPlanningPlans()
                .stream()
                .map(shiftPlan -> String.valueOf(shiftPlan.getId()))
                .toList())
            .lockedPlans(volunteer.getLockedPlans()
                .stream()
                .map(shiftPlan -> String.valueOf(shiftPlan.getId()))
                .toList())
            .build();
    }

    public static Collection<UserEventDto> toUserEventDtoForUsers(Collection<Volunteer> volunteers, Collection<DirectoryUser> users) {
        return users.stream()
            .map(u ->
                toUserEventDto(volunteers.stream()
                    .filter(v -> v.getId().equals(u.id()))
                    .findFirst()
                    .orElse(null), u)
            )
            .sorted(UserEventDto.lastNameComparator())
            .toList();
    }

    public static ContactInfoDto toContactInfoDto(DirectoryUser user) {
        return ContactInfoDto.builder()
            .userId(user.id())
            .firstName(user.firstName())
            .lastName(user.lastName())
            .email(user.email())
            .build();
    }
}
