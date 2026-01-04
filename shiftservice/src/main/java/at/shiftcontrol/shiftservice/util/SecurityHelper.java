package at.shiftcontrol.shiftservice.util;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.dao.userprofile.VolunteerDao;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityHelper {
    private final ApplicationUserProvider userProvider;
    private final VolunteerDao volunteerDao;
    private final EventDao eventDao;

    public void assertUserIsPlanner(long shiftPlanId, ShiftControlUser user) throws ForbiddenException {
        if (!isUserPlanner(shiftPlanId, user)) {
            throw new ForbiddenException("User is not a Planner in plan: " + shiftPlanId);
        }
    }

    private boolean isUserPlanner(long shiftPlanId, ShiftControlUser user) {
        return user.isPlannerInPlan(shiftPlanId);
    }

    public void assertUserIsPlanner(long shiftPlanId) throws ForbiddenException {
        assertUserIsPlanner(shiftPlanId, userProvider.getCurrentUser());
    }

    private boolean isUserPlanner(long shiftPlanId) {
        var currentUser = userProvider.getCurrentUser();
        return isUserPlanner(shiftPlanId, currentUser);
    }

    public void assertUserIsPlanner(ShiftPlan shiftPlan) throws ForbiddenException {
        assertUserIsPlanner(shiftPlan.getId());
    }

    public void assertUserIsPlannerInAnyPlanOfEvent(Event event) throws ForbiddenException {
        boolean isPlannerInAnyPlan = event.getShiftPlans()
            .stream()
            .anyMatch(shiftPlan -> isUserPlanner(shiftPlan.getId()));
        var isNotAdmin = isNotUserAdmin();

        if (!isPlannerInAnyPlan && isNotAdmin) {
            throw new ForbiddenException(
                "User has no planner access to any shift plan of event with id: " + event.getId()
            );
        }
    }

    public void assertUserIsPlannerInAnyPlanOfEvent(String eventId) throws ForbiddenException, NotFoundException {
        Event event = eventDao.findById(ConvertUtil.idToLong(eventId))
            .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));
        assertUserIsPlannerInAnyPlanOfEvent(event);
    }

    public void assertUserIsPlanner(Shift shift) throws ForbiddenException {
        assertUserIsPlanner(shift.getShiftPlan());
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

    public void assertUserIsVolunteer(ShiftPlan shiftPlan) throws ForbiddenException {
        assertUserIsVolunteer(shiftPlan.getId());
    }

    public void assertUserIsVolunteer(Shift shift) throws ForbiddenException {
        assertUserIsVolunteer(shift.getShiftPlan());
    }

    public void assertUserIsVolunteer(PositionSlot positionSlot) throws ForbiddenException {
        assertUserIsVolunteer(positionSlot.getShift());
    }
    //     --------------------- Volunteer or Planner ---------------------

    private boolean isUserInPlan(long shiftPlanId) {
        var currentUser = userProvider.getCurrentUser();
        return currentUser.isVolunteerInPlan(shiftPlanId) || currentUser.isPlannerInPlan(shiftPlanId);
    }

    public void assertUserIsInPlan(long shiftPlanId) throws ForbiddenException {
        if (!isUserInPlan(shiftPlanId)) {
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


    private boolean isUserInAnyPlanOfEvent(Event event) {
        return event.getShiftPlans()
            .stream()
            .anyMatch(shiftPlan -> isUserInPlan(shiftPlan.getId()));
    }

    public void assertUserIsAllowedToAccessEvent(Event event) throws ForbiddenException {
        boolean isInAnyPlan = isUserInAnyPlanOfEvent(event); // also false if no shift plans exist
        boolean isNotAdmin = isNotUserAdmin();

        if (!isInAnyPlan && isNotAdmin) {
            throw new ForbiddenException(
                "User has no access to any shift plan of event with id: " + event.getId()
            );
        }
    }

    //     --------------------- Admin ---------------------
    public boolean isUserAdmin() {
        var currentUser = userProvider.getCurrentUser();
        return isUserAdmin(currentUser);
    }

    public boolean isUserAdmin(ShiftControlUser user) {
        return user instanceof AdminUser;
    }

    public boolean isNotUserAdmin() {
        return !isUserAdmin();
    }

    public boolean isNotUserAdmin(ShiftControlUser user) {
        return !isUserAdmin(user);
    }
}
