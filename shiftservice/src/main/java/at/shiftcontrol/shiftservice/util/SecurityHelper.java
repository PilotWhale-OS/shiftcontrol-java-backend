package at.shiftcontrol.shiftservice.util;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;

@Service
@RequiredArgsConstructor
public class SecurityHelper {
    private final ApplicationUserProvider userProvider;
    private final VolunteerDao volunteerDao;

    public void assertUserIsPlanner(Event event, Volunteer volunteer) throws ForbiddenException {
        List<@NotNull Event> planningPlans = volunteer
            .getPlanningPlans()
            .stream()
            .map(ShiftPlan::getEvent)
            .toList();
        if (!planningPlans.contains(event)) {
            throw new ForbiddenException();
        }
    }

    public void assertUserIsPlanner(Event event) throws ForbiddenException {
        String userId = userProvider.getCurrentUser().getUserId();
        Volunteer volunteer = volunteerDao.findById(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found: " + userId));
        assertUserIsPlanner(event, volunteer);
    }

    public void assertUserIsPlanner(ShiftPlan shiftPlan, Volunteer volunteer) throws ForbiddenException {
        assertUserIsPlanner(shiftPlan.getEvent(), volunteer);
    }

    public void assertUserIsPlanner(ShiftPlan shiftPlan) throws ForbiddenException {
        assertUserIsPlanner(shiftPlan.getEvent());
    }

    public void assertUserIsPlanner(Shift shift, Volunteer volunteer) throws ForbiddenException {
        assertUserIsPlanner(shift.getShiftPlan(), volunteer);
    }

    public void assertUserIsPlanner(Shift shift) throws ForbiddenException {
        assertUserIsPlanner(shift.getShiftPlan());
    }

    public void assertUserIsPlanner(PositionSlot positionSlot, Volunteer volunteer) throws ForbiddenException {
        assertUserIsPlanner(positionSlot.getShift(), volunteer);
    }

    public void assertUserIsPlanner(PositionSlot positionSlot) throws ForbiddenException {
        assertUserIsPlanner(positionSlot.getShift());
    }

    public void asserUserIsVolunteer(Event event, Volunteer volunteer) throws ForbiddenException {
        List<@NotNull Event> volunteeringPlans = volunteer
            .getVolunteeringPlans()
            .stream()
            .map(ShiftPlan::getEvent)
            .toList();
        if (!volunteeringPlans.contains(event)) {
            throw new ForbiddenException();
        }
    }

    public void asserUserIsVolunteer(Event event) throws ForbiddenException {
        String userId = userProvider.getCurrentUser().getUserId();
        Volunteer volunteer = volunteerDao.findById(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found: " + userId));
        asserUserIsVolunteer(event, volunteer);
    }

    public void asserUserIsVolunteer(ShiftPlan shiftPlan, Volunteer volunteer) throws ForbiddenException {
        asserUserIsVolunteer(shiftPlan.getEvent(), volunteer);
    }

    public void asserUserIsVolunteer(ShiftPlan shiftPlan) throws ForbiddenException {
        asserUserIsVolunteer(shiftPlan.getEvent());
    }

    public void asserUserIsVolunteer(Shift shift, Volunteer volunteer) throws ForbiddenException {
        asserUserIsVolunteer(shift.getShiftPlan(), volunteer);
    }

    public void asserUserIsVolunteer(Shift shift) throws ForbiddenException {
        asserUserIsVolunteer(shift.getShiftPlan());
    }

    public void asserUserIsVolunteer(PositionSlot positionSlot, Volunteer volunteer) throws ForbiddenException {
        asserUserIsVolunteer(positionSlot.getShift(), volunteer);
    }

    public void asserUserIsVolunteer(PositionSlot positionSlot) throws ForbiddenException {
        asserUserIsVolunteer(positionSlot.getShift());
    }
}
