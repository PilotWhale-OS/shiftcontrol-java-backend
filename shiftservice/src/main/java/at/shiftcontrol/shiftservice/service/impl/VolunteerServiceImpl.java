package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collections;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.service.VolunteerService;
import at.shiftcontrol.shiftservice.userdirectory.LocalUserDirectoryProvisioningService;

@Service
@RequiredArgsConstructor
public class VolunteerServiceImpl implements VolunteerService {
    private final VolunteerDao volunteerDao;
    private final LocalUserDirectoryProvisioningService localUserDirectoryProvisioningService;

    @Override
    public @NonNull Volunteer createVolunteer(@NonNull String userId) {
        var newVolunteer = Volunteer.builder()
            .id(userId)
            .planningPlans(Collections.emptySet())
            .volunteeringPlans(Collections.emptySet())
            .lockedPlans(Collections.emptySet())
            .roles(Collections.emptySet())
            .notificationSettings(Collections.emptySet())
            .build();
        try {
            Volunteer volunteer = volunteerDao.save(newVolunteer);
            localUserDirectoryProvisioningService.ensureUserAccountForVolunteerId(userId);
            return volunteer;
        } catch (DataIntegrityViolationException e) {
            // another concurrent request created the same volunteer in the meantime
            Volunteer volunteer = volunteerDao.getById(userId);
            localUserDirectoryProvisioningService.ensureUserAccountForVolunteerId(userId);
            return volunteer;
        }
    }

    @Override
    public @NonNull Volunteer getOrCreate(@NonNull String userId) {
        return volunteerDao.findById(userId).orElseGet(() -> createVolunteer(userId));
    }
}
