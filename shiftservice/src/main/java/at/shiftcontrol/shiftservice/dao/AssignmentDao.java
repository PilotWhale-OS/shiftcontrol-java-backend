package at.shiftcontrol.shiftservice.dao;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import at.shiftcontrol.lib.entity.Assignment;

public interface AssignmentDao extends BasicDao<Assignment, Long> {
    Optional<Assignment> findBySlotAndUser(long positionSlotId, String assignedUser);

    Collection<Assignment> findSignupRequestsByShiftPlanId(long shiftPlanId);

    Collection<Assignment> findAuctionsByShiftPlanId(long shiftPlanId);

    Collection<Assignment> findAuctionsByShiftPlanIdExcludingUser(long shiftPlanId, String userId);

    Collection<Assignment> getConflictingAssignments(String volunteerId, Instant startTime, Instant endTime);

    Collection<Assignment> getActiveAssignmentsOfSlot(long positionSlotId);

    Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlotId);

    Assignment getAssignmentForPositionSlotAndUser(long positionSlotId, String userId);

    Collection<Assignment> getAssignmentForPositionSlotAndUsers(long positionSlotId, Collection<String> userIds);

    Collection<Assignment> findAssignmentsForShiftPlanAndUser(long shiftPlanId, String userId);

    void deleteAll(Collection<Assignment> ids);
}
