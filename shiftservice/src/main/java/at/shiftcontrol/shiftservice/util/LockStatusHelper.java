package at.shiftcontrol.shiftservice.util;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;
import at.shiftcontrol.lib.entity.PositionSlot;
import at.shiftcontrol.lib.entity.Shift;
import at.shiftcontrol.lib.entity.ShiftPlan;
import at.shiftcontrol.lib.type.LockStatus;

public class LockStatusHelper {
    //     --------------------- LOCKED ---------------------

    public static boolean isLocked(ShiftPlan shiftPlan) {
        return shiftPlan.getLockStatus() == LockStatus.LOCKED;
    }

    public static boolean isLocked(Shift shift) {
        return isLocked(shift.getShiftPlan());
    }

    public static boolean isLocked(PositionSlot positionSlot) {
        return isLocked(positionSlot.getShift());
    }

    public static boolean isLocked(Assignment assignment) {
        return isLocked(assignment.getPositionSlot());
    }

    public static boolean isLocked(AssignmentSwitchRequest switchRequest) {
        return isLocked(switchRequest.getOfferingAssignment())
            || isLocked(switchRequest.getRequestedAssignment());
    }

    //     --------------------- SUPERVISED ---------------------

    public static boolean isSupervised(ShiftPlan shiftPlan) {
        return shiftPlan.getLockStatus() == LockStatus.SUPERVISED;
    }

    public static boolean isSupervised(Shift shift) {
        return isSupervised(shift.getShiftPlan());
    }

    public static boolean isSupervised(PositionSlot positionSlot) {
        return isSupervised(positionSlot.getShift());
    }

    public static boolean isSupervised(Assignment assignment) {
        return isSupervised(assignment.getPositionSlot());
    }

    public static boolean isSupervised(AssignmentSwitchRequest switchRequest) {
        return isSupervised(switchRequest.getOfferingAssignment())
            || isSupervised(switchRequest.getRequestedAssignment());
    }

    //     --------------------- SELF_SIGNUP ---------------------

    public static boolean isSelfSignup(ShiftPlan shiftPlan) {
        return shiftPlan.getLockStatus() == LockStatus.SELF_SIGNUP;
    }

    public static boolean isSelfSignup(Shift shift) {
        return isSelfSignup(shift.getShiftPlan());
    }

    public static boolean isSelfSignup(PositionSlot positionSlot) {
        return isSelfSignup(positionSlot.getShift());
    }

    public static boolean isSelfSignup(Assignment assignment) {
        return isSelfSignup(assignment.getPositionSlot());
    }

    //     --------------------- JOIN ---------------------

    public static void assertJoinPossible(ShiftPlan shiftPlan) {
        if (isLocked(shiftPlan)) {
            throw new IllegalStateException("join not possible, shift plan is locked");
        }
        if (isSupervised(shiftPlan)) {
            throw new IllegalStateException("join not possible, shift plan is supervised");
        }
    }

    public static void assertJoinPossible(Shift shift) {
        assertJoinPossible(shift.getShiftPlan());
    }

    public static void assertJoinPossible(PositionSlot positionSlot) {
        assertJoinPossible(positionSlot.getShift());
    }

    public static void assertJoinPossible(Assignment assignment) {
        assertJoinPossible(assignment.getPositionSlot());
    }

    //     --------------------- LEAVE ---------------------

    public static void assertLeavePossible(ShiftPlan shiftPlan) {
        if (isLocked(shiftPlan)) {
            throw new IllegalStateException("leave not possible, shift plan is locked");
        }
        if (isSupervised(shiftPlan)) {
            throw new IllegalStateException("leave not possible, shift plan is supervised");
        }
    }

    public static void assertLeavePossible(Shift shift) {
        assertLeavePossible(shift.getShiftPlan());
    }

    public static void assertLeavePossible(PositionSlot positionSlot) {
        assertLeavePossible(positionSlot.getShift());
    }

    public static void assertLeavePossible(Assignment assignment) {
        assertLeavePossible(assignment.getPositionSlot());
    }

    //     --------------------- JOIN REQUEST ---------------------

    public static void assertJoinRequestPossible(ShiftPlan shiftPlan) {
        if (isLocked(shiftPlan)) {
            throw new IllegalStateException("join request not possible, shift plan is locked");
        }
        if (isSelfSignup(shiftPlan)) {
            throw new IllegalStateException("join request not possible, shift plan is self signup");
        }
    }

    public static void assertJoinRequestPossible(Shift shift) {
        assertJoinRequestPossible(shift.getShiftPlan());
    }

    public static void assertJoinRequestPossible(PositionSlot positionSlot) {
        assertJoinRequestPossible(positionSlot.getShift());
    }

    public static void assertJoinRequestPossible(Assignment assignment) {
        assertJoinRequestPossible(assignment.getPositionSlot());
    }

    //     --------------------- LEAVE REQUEST ---------------------

    public static void assertLeaveRequestPossible(ShiftPlan shiftPlan) {
        if (isLocked(shiftPlan)) {
            throw new IllegalStateException("leave request not possible, shift plan is locked");
        }
        if (isSelfSignup(shiftPlan)) {
            throw new IllegalStateException("leave request not possible, shift plan is self signup");
        }
    }

    public static void assertLeaveRequestPossible(Shift shift) {
        assertLeaveRequestPossible(shift.getShiftPlan());
    }

    public static void assertLeaveRequestPossible(PositionSlot positionSlot) {
        assertLeaveRequestPossible(positionSlot.getShift());
    }

    public static void assertLeaveRequestPossible(Assignment assignment) {
        assertLeaveRequestPossible(assignment.getPositionSlot());
    }
}
