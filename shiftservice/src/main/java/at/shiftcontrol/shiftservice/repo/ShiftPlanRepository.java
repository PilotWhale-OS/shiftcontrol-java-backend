package at.shiftcontrol.shiftservice.repo;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.ShiftPlan;

@Repository
public interface ShiftPlanRepository extends JpaRepository<ShiftPlan, Long>, JpaSpecificationExecutor<ShiftPlan> {
    @Query("SELECT sp FROM ShiftPlan sp WHERE sp.event.id = :eventId")
    Collection<ShiftPlan> findByEventId(Long eventId);
}
