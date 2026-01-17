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

@RequiredArgsConstructor
@Service
public class UserAssemblingMapper {
    private final KeycloakUserService keycloakUserService;

    public UserEventDto toUserEventDto(Volunteer volunteer) {
        var user = keycloakUserService.getUserById(volunteer.getId());
        return UserAssemblingMapper.toUserEventDto(volunteer, user);
    }

    public static UserEventDto toUserEventDto(Volunteer volunteer, UserRepresentation user) {
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

    public static Collection<UserEventDto> toUserEventDto(Collection<Volunteer> volunteer, Collection<UserRepresentation> user) {
        return volunteer.stream()
            .map(v ->
                toUserEventDto(v, user.stream()
                    .filter(u -> u.getId().equals(v.getId()))
                    .findFirst()
                    .orElseThrow(NotFoundException::new)))
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
