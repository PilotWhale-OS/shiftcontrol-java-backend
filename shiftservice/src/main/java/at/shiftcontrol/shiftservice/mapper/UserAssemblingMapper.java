package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.dto.user.ContactInfoDto;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.dto.user.UserPlanDto;

@RequiredArgsConstructor
@Service
public class UserAssemblingMapper {
    private final KeycloakUserService keycloakUserService;

    public UserPlanDto toUserPlanDto(Volunteer volunteer, Long planId) {
        var user = keycloakUserService.getUserById(volunteer.getId());
        return UserAssemblingMapper.toUserPlanDto(volunteer, user, planId);
    }

    public static UserPlanDto toUserPlanDto(Volunteer volunteer, UserRepresentation user, Long planId) {
        return UserPlanDto.builder()
            .volunteer(user == null ? null : VolunteerAssemblingMapper.toDtoFromUser(user))
            .email(user == null ? null : user.getEmail())
            .roles(volunteer == null ? null : RoleMapper.toRoleDto(volunteer.getRoles(), planId))
            .isLocked(volunteer == null ? null : volunteer.getLockedPlans().stream().anyMatch(x -> planId.equals(x.getId())))
            .build();
    }

    public static Collection<UserPlanDto> toUserPlanDto(Collection<Volunteer> volunteers, Collection<UserRepresentation> users, Long planId) {
        return volunteers.stream()
            .map(v ->
                toUserPlanDto(v, users.stream()
                        .filter(u -> u.getId().equals(v.getId()))
                        .findFirst().orElse(null),
                    planId)
            )
            .toList();
    }

    public UserEventDto toUserEventDto(Volunteer volunteer) {
        var user = keycloakUserService.getUserById(volunteer.getId());
        return UserAssemblingMapper.toUserEventDto(volunteer, user);
    }

    public static UserEventDto toUserEventDto(Volunteer volunteer, UserRepresentation user) {
        if (volunteer == null) {
            return UserEventDto.builder()
                .volunteer(VolunteerAssemblingMapper.toDtoFromUser(user))
                .email(user.getEmail())
                .build();
        }
        return UserEventDto.builder()
            .volunteer(VolunteerAssemblingMapper.toDtoFromUser(user))
            .email(user.getEmail())
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

    public static Collection<UserEventDto> toUserEventDto(Collection<Volunteer> volunteers, Collection<UserRepresentation> users) {
        return volunteers.stream()
            .map(v ->
                toUserEventDto(v, users.stream()
                    .filter(u -> u.getId().equals(v.getId()))
                    .findFirst()
                    .orElseThrow(NotFoundException::new)))
            .toList();
    }

    public static Collection<UserEventDto> toUserEventDtoForUsers(Collection<Volunteer> volunteers, Collection<UserRepresentation> users) {
        return users.stream()
            .map(u ->
                toUserEventDto(volunteers.stream()
                    .filter(v -> v.getId().equals(u.getId()))
                    .findFirst()
                    .orElse(null), u)
            )
            .toList();
    }

    public static ContactInfoDto toContactInfoDto(UserRepresentation user) {
        return ContactInfoDto.builder()
            .userId(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .build();
    }
}
