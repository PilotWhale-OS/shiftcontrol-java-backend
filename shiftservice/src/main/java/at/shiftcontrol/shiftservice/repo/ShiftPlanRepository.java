package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.shiftservice.entity.ShiftPlan;

@Repository
public interface ShiftPlanRepository extends JpaRepository<ShiftPlan, Long> {
}
