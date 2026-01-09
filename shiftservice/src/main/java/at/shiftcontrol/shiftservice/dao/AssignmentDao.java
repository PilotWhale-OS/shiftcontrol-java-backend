package at.shiftcontrol.shiftservice.dao;

import java.time.Instant;
import java.util.Collection;

import at.shiftcontrol.lib.entity.Assignment;
import at.shiftcontrol.lib.entity.AssignmentId;

public interface AssignmentDao extends BasicDao<Assignment, AssignmentId> {
    Collection<Assignment> findAuctionsByShiftPlanId(long shiftPlanId);

    Collection<Assignment> findAuctionsByShiftPlanIdExcludingUser(long shiftPlanId, String userId);

    Collection<Assignment> getConflictingAssignments(String volunteerId, Instant startTime, Instant endTime);

    Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlotId);

    Assignment getAssignmentForPositionSlotAndUser(long positionSlotId, String userId);

    Collection<Assignment> findAssignmentsForShiftPlanAndUser(long shiftPlanId, String userId);

    void deleteAll(Collection<Assignment> ids);
}
