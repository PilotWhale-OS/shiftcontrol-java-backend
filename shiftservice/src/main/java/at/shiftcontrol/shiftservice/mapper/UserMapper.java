package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.user.UserEventDto;
import at.shiftcontrol.shiftservice.entity.Volunteer;

@RequiredArgsConstructor
@Service
public class UserMapper {
    public static UserEventDto toUserEventDto(Volunteer volunteer, UserRepresentation user) {
        return UserEventDto.builder()
            .id(user.getId()) // todo change to volunteer dto and use mapper
            .fistName(user.getFirstName())
            .lastName(user.getLastName())
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
}
