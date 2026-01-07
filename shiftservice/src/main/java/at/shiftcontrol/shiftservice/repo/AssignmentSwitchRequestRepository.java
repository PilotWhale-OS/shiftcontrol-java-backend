package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@Repository
public interface AssignmentSwitchRequestRepository extends JpaRepository<AssignmentSwitchRequest, AssignmentSwitchRequestId> {
    @Modifying
    @Query("""
            UPDATE AssignmentSwitchRequest a
            SET a.status = :status
            WHERE EXISTS (
                SELECT 1
                FROM Assignment oa
                WHERE oa = a.offeringAssignment
                  AND oa.positionSlot.id = :positionSlotId
                  AND oa.assignedVolunteer.id = :assignedUser
            )
            OR EXISTS (
                SELECT 1
                FROM Assignment ra
                WHERE ra = a.requestedAssignment
                  AND ra.positionSlot.id = :positionSlotId
                  AND ra.assignedVolunteer.id = :assignedUser
            )
        """)
    void cancelTradesForAssignment(Long positionSlotId, String assignedUser, TradeStatus status);

    @Query("""
            SELECT a FROM AssignmentSwitchRequest a
            WHERE a.requestedAssignment.positionSlot.id = :positionSlotId
            AND a.offeringAssignment.assignedVolunteer.id = :userId
            AND a.status = :status
        """)
    Collection<AssignmentSwitchRequest> findOpenTradesForRequestedPositionAndOfferingUser(long positionSlotId, String userId, TradeStatus status);

    @Query("""
            SELECT a FROM AssignmentSwitchRequest a
            WHERE (a.offeringAssignment.positionSlot.shift.shiftPlan.id = :shiftPlanId
            AND a.offeringAssignment.assignedVolunteer.id = :userId)
            OR (a.requestedAssignment.positionSlot.shift.shiftPlan.id = :shiftPlanId
            AND a.requestedAssignment.assignedVolunteer.id = :userId)
        """)
    Collection<AssignmentSwitchRequest> findTradesForShiftPlanAndUser(long shiftPlanId, String userId);

    @Modifying
    @Query("""
            DELETE FROM AssignmentSwitchRequest a
            WHERE a.offeringAssignment.positionSlot.id = :positionSlotId
            AND a.requestedAssignment.assignedVolunteer.id = :userId
        """)
    void deleteTradesForOfferedPositionAndRequestedUser(long positionSlotId, String userId);
}
