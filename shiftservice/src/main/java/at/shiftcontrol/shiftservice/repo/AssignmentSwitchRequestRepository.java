package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@Repository
public interface AssignmentSwitchRequestRepository extends JpaRepository<AssignmentSwitchRequest, AssignmentSwitchRequestId> {
    @Modifying(clearAutomatically = true)
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
}
