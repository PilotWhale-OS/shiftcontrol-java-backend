package at.shiftcontrol.shiftservice.service.user;

import at.shiftcontrol.lib.entity.Volunteer;

public interface VolunteerService {

    Volunteer createVolunteer(String userId);

    Volunteer getOrCreate(String userId);
}
