package at.shiftcontrol.shiftservice.mapper;

import java.util.Collection;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;

public class VolunteerMapper {
    public static VolunteerDto toDto(Volunteer volunteer) {
        return new VolunteerDto(volunteer.getId());
    }

    public static Collection<VolunteerDto> toDto(java.util.Collection<Volunteer> volunteers) {
        return volunteers.stream().map(VolunteerMapper::toDto).toList();
    }
}
