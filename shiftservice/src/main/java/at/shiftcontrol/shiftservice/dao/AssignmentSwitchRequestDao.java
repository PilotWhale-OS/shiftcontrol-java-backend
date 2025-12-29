package at.shiftcontrol.shiftservice.dao;

import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;

public interface AssignmentSwitchRequestDao extends  BasicDao<AssignmentSwitchRequest, AssignmentSwitchRequestId> {

    void cancelTradesForAssignment(Long positionSlotId, String assignedUser);

    Collection<AssignmentSwitchRequest> getOpenTradesForRequestedPositionAndOfferingUser(long positionSlotId, String userId);

}
