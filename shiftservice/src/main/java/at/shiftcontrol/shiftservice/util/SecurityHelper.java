package at.shiftcontrol.shiftservice.util;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import at.shiftcontrol.lib.exception.ForbiddenException;
import at.shiftcontrol.lib.util.ConvertUtil;
import at.shiftcontrol.shiftservice.auth.ApplicationUserProvider;
import at.shiftcontrol.shiftservice.auth.user.AdminUser;
import at.shiftcontrol.shiftservice.auth.user.ShiftControlUser;
import at.shiftcontrol.shiftservice.dao.EventDao;
import at.shiftcontrol.shiftservice.entity.Event;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityHelper {
    private final ApplicationUserProvider userProvider;
    private final EventDao eventDao;

    public boolean isUserPlanner(long shiftPlanId, ShiftControlUser user) {
        return user.isPlannerInPlan(shiftPlanId);
    }

    public boolean isUserPlanner(long shiftPlanId) {
        var currentUser = userProvider.getCurrentUser();
        return isUserPlanner(shiftPlanId, currentUser);
    }

    public void assertUserIsPlanner(long shiftPlanId) {
        assertUserIsPlanner(shiftPlanId, userProvider.getCurrentUser());
    }

    public void assertUserIsPlanner(long shiftPlanId, ShiftControlUser user) {
        if (!isUserPlanner(shiftPlanId, user)) {
            log.error("User is not a planner in shift plan with id: {}", shiftPlanId);
            throw new ForbiddenException("User is not a planner in shift plan.");
        }
    }

    public void assertUserIsPlanner(ShiftPlan shiftPlan) {
        assertUserIsPlanner(shiftPlan.getId());
    }

    public void assertUserIsPlanner(Shift shift) {
        assertUserIsPlanner(shift.getShiftPlan());
    }

    public void assertUserIsPlanner(PositionSlot positionSlot) {
        assertUserIsPlanner(positionSlot.getShift());
    }

    public void assertUserIsPlannerInAnyPlanOfEvent(Event event) {
        boolean isPlannerInAnyPlan = event.getShiftPlans()
            .stream()
            .anyMatch(shiftPlan -> isUserPlanner(shiftPlan.getId()));
        var isNotAdmin = isNotUserAdmin();
        if (!isPlannerInAnyPlan && isNotAdmin) {
            log.error("User has no planner access to any shift plan of event with id: {}", event.getId());
            throw new ForbiddenException("User has no planner access to any shift plan of event.");
        }
    }

    public void assertUserIsPlannerInAnyPlanOfEvent(String eventId) {
        Event event = eventDao.getById(ConvertUtil.idToLong(eventId));
        assertUserIsPlannerInAnyPlanOfEvent(event);
    }
    //     --------------------- Volunteer ---------------------

    public boolean isVolunteerInPlan(long shiftPlanId, ShiftControlUser user) {
        return user.isVolunteerInPlan(shiftPlanId);
    }

    public boolean isVolunteerInPlan(long shiftPlanId) {
        var currentUser = userProvider.getCurrentUser();
        return isVolunteerInPlan(shiftPlanId, currentUser);
    }

    public boolean isUserOnlyVolunteerInPlan(long shiftPlanId) {
        var currentUser = userProvider.getCurrentUser();
        return isVolunteerInPlan(shiftPlanId, currentUser) && !isUserPlanner(shiftPlanId, currentUser);
    }

    public void assertUserIsVolunteer(long shiftPlanId, ShiftControlUser user) {
        if (!isVolunteerInPlan(shiftPlanId, user)) {
            log.error("User is not a volunteer in plan with id: {}", shiftPlanId);
            throw new ForbiddenException("User is not a volunteer in plan.");
        }
    }

    public void assertUserIsVolunteer(long shiftPlanId) {
        assertUserIsVolunteer(shiftPlanId, userProvider.getCurrentUser());
    }

    public void assertUserIsVolunteer(ShiftPlan shiftPlan) {
        assertUserIsVolunteer(shiftPlan.getId());
    }

    public void assertUserIsVolunteer(Shift shift) {
        assertUserIsVolunteer(shift.getShiftPlan());
    }

    public void assertUserIsVolunteer(PositionSlot positionSlot) {
        assertUserIsVolunteer(positionSlot.getShift());
    }
    //     --------------------- Volunteer or Planner ---------------------

    private boolean isUserInPlan(long shiftPlanId) {
        var currentUser = userProvider.getCurrentUser();
        return currentUser.isVolunteerInPlan(shiftPlanId) || currentUser.isPlannerInPlan(shiftPlanId);
    }

    public void assertUserIsInPlan(long shiftPlanId) {
        if (!isUserInPlan(shiftPlanId)) {
            log.error("User has no access to shift plan with id: {}", shiftPlanId);
            throw new ForbiddenException("User has no access to shift plan.");
        }
    }

    public void assertUserIsInPlan(ShiftPlan shiftPlan) {
        assertUserIsInPlan(shiftPlan.getId());
    }

    public void assertUserIsInPlan(Shift shift) {
        assertUserIsInPlan(shift.getShiftPlan());
    }

    public void assertUserIsInPlan(PositionSlot positionSlot) {
        assertUserIsInPlan(positionSlot.getShift());
    }

    private boolean isUserInAnyPlanOfEvent(Event event) {
        return event.getShiftPlans()
            .stream()
            .anyMatch(shiftPlan -> isUserInPlan(shiftPlan.getId()));
    }

    public void assertUserIsAllowedToAccessEvent(Event event) {
        boolean isInAnyPlan = isUserInAnyPlanOfEvent(event); // also false if no shift plans exist
        boolean isNotAdmin = isNotUserAdmin();
        if (!isInAnyPlan && isNotAdmin) {
            log.error("User has no access to any shift plan of event with id: {}", event.getId());
            throw new ForbiddenException("User has no access to any shift plan of event.");
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
