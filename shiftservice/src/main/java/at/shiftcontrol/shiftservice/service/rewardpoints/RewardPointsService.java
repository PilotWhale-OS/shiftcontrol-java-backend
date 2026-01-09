package at.shiftcontrol.shiftservice.service.rewardpoints;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.exception.ConflictException;

public interface RewardPointsService {
    void onAssignmentAccepted(Assignment assignment) throws ConflictException;

    void onAssignmentCreated(Assignment assignment, String acceptedRewardPointsHash) throws ConflictException;

    void onAssignmentRemoved(Assignment assignment);

    void onAssignmentReassignedAuction(Assignment oldAssignment, Assignment newAssignment, String acceptedRewardPointsHash)
        throws ConflictException;

    void onAssignmentReassignedTrade(Assignment oldAssignment, Assignment newAssignment)
        throws ConflictException;

    void manualAdjust(String volunteerId, long eventId, int points, String reason);
}
