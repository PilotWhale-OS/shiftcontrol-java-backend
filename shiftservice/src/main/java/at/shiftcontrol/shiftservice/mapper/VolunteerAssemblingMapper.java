package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.auth.KeycloakUserService;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;

@RequiredArgsConstructor
@Service
public class VolunteerAssemblingMapper {
    private final KeycloakUserService keycloakUserService;

    public VolunteerDto toDto(Volunteer volunteer) {
        var user = keycloakUserService.getUserById(volunteer.getId());
        return toDtoFromUser(user);
    }

    public Collection<VolunteerDto> toDto(Collection<Volunteer> volunteers) {
        var users = keycloakUserService.getUserByIds(volunteers.stream().map(Volunteer::getId).toList());
        return toDtoFromUser(users);
    }

    public static VolunteerDto toDtoFromUser(UserRepresentation user) {
        return VolunteerDto.builder()
            .id(user.getId())
            .fistName(user.getFirstName())
            .lastName(user.getLastName())
            .build();
    }

    public static Collection<VolunteerDto> toDtoFromUser(Collection<UserRepresentation> user) {
        return user.stream().map(VolunteerAssemblingMapper::toDtoFromUser).toList();
    }
}
