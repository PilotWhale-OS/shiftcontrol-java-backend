package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.Assignment;
import at.shiftcontrol.shiftservice.entity.AssignmentId;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, AssignmentId> {
    @Query("SELECT a FROM Assignment a "
        + "WHERE a.positionSlot.shift.shiftPlan.id = :spId AND a.status IN ('AUCTION_REQUEST_FOR_UNASSIGN', 'AUCTION')")
    Collection<Assignment> findAuctionsByShiftPlanId(long spId);
}
