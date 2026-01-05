package at.shiftcontrol.shiftservice.service.rewardpoints;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.shiftservice.entity.Assignment;

public interface RewardPointsService {
    void onAssignmentCreated(Assignment assignment, String acceptedRewardPointsHash) throws ConflictException;

    void onAssignmentRemoved(Assignment assignment);

    void onAssignmentReassigned(Assignment oldAssignment, Assignment newAssignment, String acceptedRewardPointsHash)
        throws ConflictException;

    void manualAdjust(String volunteerId, long eventId, int points, String reason);
}
