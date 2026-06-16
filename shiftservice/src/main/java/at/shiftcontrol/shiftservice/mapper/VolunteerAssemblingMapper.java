package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

@RequiredArgsConstructor
@Service
public class VolunteerAssemblingMapper {
    private final UserDirectoryService userDirectoryService;

    public VolunteerDto toDto(Volunteer volunteer) {
        var user = userDirectoryService.getUserById(volunteer.getId());
        return toDtoFromUser(user);
    }

    public Collection<VolunteerDto> toDto(Collection<Volunteer> volunteers) {
        var users = userDirectoryService.getUserByIds(volunteers.stream().map(Volunteer::getId).toList());
        return toDtoFromUser(users);
    }

    public static VolunteerDto toDtoFromUser(DirectoryUser user) {
        return VolunteerDto.builder()
            .id(user.id())
            .firstName(user.firstName())
            .lastName(user.lastName())
            .build();
    }

    public static Collection<VolunteerDto> toDtoFromUser(Collection<DirectoryUser> users) {
        return users.stream().map(VolunteerAssemblingMapper::toDtoFromUser).toList();
    }
}
