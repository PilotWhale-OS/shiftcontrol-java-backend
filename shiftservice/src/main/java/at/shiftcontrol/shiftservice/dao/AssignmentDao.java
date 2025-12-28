package at.shiftcontrol.shiftservice.dao;

import java.time.Instant;
import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;

public interface AssignmentDao extends BasicDao<Assignment, AssignmentId> {
    Collection<Assignment> findAuctionsByShiftPlanId(long shiftPlanId);

    Collection<Assignment> getConflictingAssignments(String volunteerId, Instant startTime, Instant endTime);

    Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlotId);

    Assignment findAssignmentForPositionSlotAndUser(long positionSlotId, String userId);

    Collection<Assignment> findAssignmentsForShiftPlanAndUser(long shiftPlanId, String userId);
}
