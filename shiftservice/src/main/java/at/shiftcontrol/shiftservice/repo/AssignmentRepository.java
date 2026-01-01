package at.shiftcontrol.shiftservice.repo;

import java.time.Instant;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, AssignmentId> {
    // TODO delete after trade and auction are fully implemented and method is still not used
    @Query("""
        SELECT a FROM Assignment a
        WHERE a.positionSlot.shift.shiftPlan.id = :spId AND a.status IN ('AUCTION_REQUEST_FOR_UNASSIGN', 'AUCTION')
        """)
    Collection<Assignment> findAuctionsByShiftPlanId(long spId);

    @Query("""
        SELECT a FROM Assignment a
        WHERE a.positionSlot.shift.shiftPlan.id = :spId
        AND a.status IN ('AUCTION_REQUEST_FOR_UNASSIGN', 'AUCTION')
        AND a.assignedVolunteer.id != :userId
        """)
    Collection<Assignment> findAuctionsByShiftPlanIdExcludingUser(long spId, String userId);

    @Query("""
        SELECT a FROM Assignment a
        WHERE a.assignedVolunteer.id = :volunteerId
        AND a.positionSlot.shift.endTime > :startTime
        AND a.positionSlot.shift.startTime < :endTime
        """)
    Collection<Assignment> getConflictingAssignments(String volunteerId, Instant startTime, Instant endTime);

    @Query("""
        SELECT a FROM Assignment a
        WHERE a.assignedVolunteer.id = :volunteerId
        AND a.positionSlot.id <> :positionSlotId
        AND a.positionSlot.shift.endTime > :startTime
        AND a.positionSlot.shift.startTime < :endTime
        """)
    Collection<Assignment> getConflictingAssignmentsExcludingSlot(String volunteerId, Instant startTime, Instant endTime, long positionSlotId);

    @Query("""
        SELECT a FROM Assignment a
        WHERE a.positionSlot.id = :positionSlotId AND a.assignedVolunteer.id = :userId
        """)
    Assignment findAssignmentForPositionSlotAndUser(long positionSlotId, String userId);

    @Query("""
        SELECT a FROM Assignment a
        WHERE a.positionSlot.shift.shiftPlan.id = :shiftPlanId AND a.assignedVolunteer.id = :userId
        """)
    Collection<Assignment> findAssignmentsForShiftPlanAndUser(long shiftPlanId, String userId);
}
