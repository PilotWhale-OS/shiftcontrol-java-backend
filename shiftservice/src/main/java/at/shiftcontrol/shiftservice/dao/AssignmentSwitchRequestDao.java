package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.lib.entity.AssignmentPair;
import at.shiftcontrol.lib.entity.AssignmentSwitchRequest;

public interface AssignmentSwitchRequestDao extends  BasicDao<AssignmentSwitchRequest, Long> {
    Optional<AssignmentSwitchRequest> findByAssignmentIds(long offeredAssignmentId, long requestedAssignmentId);

    Optional<AssignmentSwitchRequest> findBySlotsAndUsers(long offeredSlotId, String offeringUserId,
                                                          long requestedSlotId, String requestedUserId);

    void cancelTradesForAssignment(Long positionSlotId, String assignedUser);

    Collection<AssignmentSwitchRequest> findOpenTradesForRequestedPositionAndOfferingUser(long positionSlotId, String userId);

    Collection<AssignmentSwitchRequest> findTradesForShiftPlanAndUser(long shiftPlanId, String userId);

    void cancelTradesForOfferedPositionAndRequestedUser(long positionSlotId, String userId);

    Optional<AssignmentSwitchRequest> findInverseTrade(AssignmentSwitchRequest trade);

    Collection<AssignmentSwitchRequest> findAllByAssignmentPairs(Collection<AssignmentPair> pairs);
}
