package at.shiftcontrol.shiftservice.mapper;

import at.shiftcontrol.shiftservice.dto.UserProfile.VolunteerDto;
import at.shiftcontrol.shiftservice.entity.Volunteer;

public class VolunteerMapper {
    public static VolunteerDto toDto(Volunteer volunteer) {
        return new VolunteerDto(
            volunteer.getId(),
            volunteer.getUsername(),
            volunteer.getEmail()
        );
    }
}
