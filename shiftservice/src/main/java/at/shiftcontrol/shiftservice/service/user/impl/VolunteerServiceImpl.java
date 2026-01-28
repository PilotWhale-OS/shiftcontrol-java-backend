package at.shiftcontrol.shiftservice.service.user.impl;

import java.util.Collections;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.entity.Volunteer;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.service.user.VolunteerService;

@Service
@RequiredArgsConstructor
public class VolunteerServiceImpl implements VolunteerService {
    private final VolunteerDao volunteerDao;

    @Override
    public Volunteer createVolunteer(String userId) {
        var newVolunteer = Volunteer.builder()
            .id(userId)
            .planningPlans(Collections.emptySet())
            .volunteeringPlans(Collections.emptySet())
            .lockedPlans(Collections.emptySet())
            .roles(Collections.emptySet())
            .notificationSettings(Collections.emptySet())
            .build();
        try {
            return volunteerDao.save(newVolunteer);
        } catch (DataIntegrityViolationException e) {
            // another concurrent request created the same volunteer in the meantime
            return volunteerDao.getById(userId);
        }
    }

    @Override
    public Volunteer getOrCreate(String userId) {
        return volunteerDao.findById(userId).orElseGet(() -> createVolunteer(userId));
    }
}
