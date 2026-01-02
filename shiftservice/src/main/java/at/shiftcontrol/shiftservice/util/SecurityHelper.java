package at.shiftcontrol.shiftservice.util;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.entity.Volunteer;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityHelper {
    private final ApplicationUserProvider userProvider;
    private final VolunteerDao volunteerDao;

    public void assertUserIsPlanner(long shiftPlanId, ShiftControlUser user) throws ForbiddenException {
        if (!user.isPlannerInPlan(shiftPlanId)) {
            throw new ForbiddenException("User is not a Planner in plan: " + shiftPlanId);
        }
    }

    public void assertUserIsPlanner(long shiftPlanId) throws ForbiddenException {
        assertUserIsPlanner(shiftPlanId, userProvider.getCurrentUser());
    }

    public void assertUserIsPlanner(ShiftPlan shiftPlan, Volunteer volunteer) throws ForbiddenException {
        if (!volunteer.getPlanningPlans().contains(shiftPlan)) {
            throw new ForbiddenException();
        }
    }

    public void assertUserIsPlanner(ShiftPlan shiftPlan, String userId) throws ForbiddenException {
        Volunteer volunteer = volunteerDao.findById(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found: " + userId));
        assertUserIsPlanner(shiftPlan, volunteer);
    }

    public void assertUserIsPlanner(ShiftPlan shiftPlan) throws ForbiddenException {
        assertUserIsPlanner(shiftPlan.getId());
    }

    public void assertUserIsPlanner(Shift shift, Volunteer volunteer) throws ForbiddenException {
        assertUserIsPlanner(shift.getShiftPlan(), volunteer);
    }

    public void assertUserIsPlanner(Shift shift, String userId) throws ForbiddenException {
        assertUserIsPlanner(shift.getShiftPlan(), userId);
    }

    public void assertUserIsPlanner(Shift shift) throws ForbiddenException {
        assertUserIsPlanner(shift.getShiftPlan());
    }

    public void assertUserIsPlanner(PositionSlot positionSlot, Volunteer volunteer) throws ForbiddenException {
        assertUserIsPlanner(positionSlot.getShift(), volunteer);
    }

    public void assertUserIsPlanner(PositionSlot positionSlot, String userId) throws ForbiddenException {
        assertUserIsPlanner(positionSlot.getShift(), userId);
    }

    public void assertUserIsPlanner(PositionSlot positionSlot) throws ForbiddenException {
        assertUserIsPlanner(positionSlot.getShift());
    }
    //     --------------------- Volunteer ---------------------

    public void assertUserIsVolunteer(long shiftPlanId, ShiftControlUser user) throws ForbiddenException {
        if (!user.isVolunteerInPlan(shiftPlanId)) {
            throw new ForbiddenException("User is not a volunteer in plan: " + shiftPlanId);
        }
    }

    public void assertUserIsVolunteer(long shiftPlanId) throws ForbiddenException {
        assertUserIsVolunteer(shiftPlanId, userProvider.getCurrentUser());
    }

    public void assertUserIsVolunteer(ShiftPlan shiftPlan, Volunteer volunteer) throws ForbiddenException {
        if (!volunteer.getVolunteeringPlans().contains(shiftPlan)) {
            throw new ForbiddenException();
        }
    }

    public void assertUserIsVolunteer(ShiftPlan shiftPlan, String userId) throws ForbiddenException {
        Volunteer volunteer = volunteerDao.findById(userId)
            .orElseThrow(() -> new NotFoundException("Volunteer not found: " + userId));
        assertUserIsVolunteer(shiftPlan, volunteer);
    }

    public void assertUserIsVolunteer(ShiftPlan shiftPlan) throws ForbiddenException {
        assertUserIsVolunteer(shiftPlan.getId());
    }

    public void assertUserIsVolunteer(Shift shift, Volunteer volunteer) throws ForbiddenException {
        assertUserIsVolunteer(shift.getShiftPlan(), volunteer);
    }

    public void assertUserIsVolunteer(Shift shift, String userId) throws ForbiddenException {
        assertUserIsVolunteer(shift.getShiftPlan(), userId);
    }

    public void assertUserIsVolunteer(Shift shift) throws ForbiddenException {
        assertUserIsVolunteer(shift.getShiftPlan());
    }

    public void assertUserIsVolunteer(PositionSlot positionSlot, Volunteer volunteer) throws ForbiddenException {
        assertUserIsVolunteer(positionSlot.getShift(), volunteer);
    }

    public void assertUserIsVolunteer(PositionSlot positionSlot, String userId) throws ForbiddenException {
        assertUserIsVolunteer(positionSlot.getShift(), userId);
    }

    public void assertUserIsVolunteer(PositionSlot positionSlot) throws ForbiddenException {
        assertUserIsVolunteer(positionSlot.getShift());
    }
    //     --------------------- Volunteer or Planner ---------------------

    public void assertUserIsInPlan(long shiftPlanId) throws ForbiddenException {
        var currentUser = userProvider.getCurrentUser();
        if (!(currentUser.isVolunteerInPlan(shiftPlanId) || currentUser.isPlannerInPlan(shiftPlanId))) {
            throw new ForbiddenException("User has no access to shift plan with id: " + shiftPlanId);
        }
    }

    public void assertUserIsInPlan(ShiftPlan shiftPlan) throws ForbiddenException {
        assertUserIsInPlan(shiftPlan.getId());
    }

    public void assertUserIsInPlan(Shift shift) throws ForbiddenException {
        assertUserIsInPlan(shift.getShiftPlan());
    }

    public void assertUserIsInPlan(PositionSlot positionSlot) throws ForbiddenException {
        assertUserIsInPlan(positionSlot.getShift());
    }


    public void assertUserIsinAnyPlanOfEvent(Event event) throws ForbiddenException {
        var plans = event.getShiftPlans();

        // call existing assertUserIsinPlan method for each plan
        for (var plan : plans) {
            try {
                assertUserIsinPlan(plan.getId());
                return; // if no exception, user has access
            } catch (ForbiddenException e) {
                // continue checking other plans
            }
        }
        throw new ForbiddenException("User has no access to any shift plan of event with id: " + event.getId());
    }
}
