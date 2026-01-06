package at.shiftcontrol.shiftservice.util;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;
import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import at.shiftcontrol.shiftservice.type.LockStatus;

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
}
