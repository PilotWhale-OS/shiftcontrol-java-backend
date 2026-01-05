package at.shiftcontrol.shiftservice.service.rewardpoints;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.PositionSlot;
import at.shiftcontrol.shiftservice.entity.Shift;

public interface RewardPointsService {
    void onAssignmentCreated(Assignment assignment, PositionSlot slot, Shift shift);

    void onAssignmentRemoved(Assignment assignment, PositionSlot slot, Shift shift);

    void onAssignmentReassigned(Assignment oldAssignment, Assignment newAssignment, PositionSlot slot, Shift shift);

    void manualAdjust(String volunteerId, long eventId, int points, String reason);
}
