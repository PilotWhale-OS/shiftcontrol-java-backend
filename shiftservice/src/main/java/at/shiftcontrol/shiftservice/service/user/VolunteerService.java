package at.shiftcontrol.shiftservice.service.user;

import at.shiftcontrol.lib.entity.Volunteer;

public interface VolunteerService {

    Volunteer createVolunteer(String id);

    Volunteer getOrCreate(String userId);
}
