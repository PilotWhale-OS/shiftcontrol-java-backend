package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.entity.Volunteer;

public interface VolunteerService {

    Volunteer createVolunteer(String userId);

    Volunteer getOrCreate(String userId);
}
