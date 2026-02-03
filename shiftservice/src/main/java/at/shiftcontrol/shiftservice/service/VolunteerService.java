package at.shiftcontrol.shiftservice.service;

import at.shiftcontrol.lib.entity.Volunteer;

import lombok.NonNull;

public interface VolunteerService {

    @NonNull
    Volunteer createVolunteer(@NonNull String userId);

    @NonNull Volunteer getOrCreate(@NonNull String userId);
}
