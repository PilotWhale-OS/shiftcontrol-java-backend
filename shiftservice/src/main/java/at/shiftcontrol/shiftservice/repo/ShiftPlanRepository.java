package at.shiftcontrol.shiftservice.repo;

import at.shiftcontrol.shiftservice.entity.ShiftPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftPlanRepository extends JpaRepository<ShiftPlan, Long> {

}