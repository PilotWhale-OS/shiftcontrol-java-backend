package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VolunteerAssemblingMapper {
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
