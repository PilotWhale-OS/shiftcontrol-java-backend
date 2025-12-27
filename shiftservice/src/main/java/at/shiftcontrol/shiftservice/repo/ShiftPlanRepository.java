package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;

import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftPlanRepository extends JpaRepository<ShiftPlan, Long>, JpaSpecificationExecutor<ShiftPlan> {
    @Query("SELECT sp FROM ShiftPlan sp WHERE sp.event.id = :eventId")
    Collection<ShiftPlan> findByEventId(Long eventId);

    @Query("""
        SELECT DISTINCT sp
        FROM ShiftPlan sp
        LEFT JOIN sp.planPlanners planner
        LEFT JOIN sp.planVolunteers volunteer
        WHERE planner.id = :userId OR volunteer.id = :userId
        """)
    Collection<ShiftPlan> findAllUserRelatedShiftPlans(String userId);
}
