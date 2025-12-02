package at.shiftcontrol.shiftservice.repo;

import at.shiftcontrol.shiftservice.entity.PositionSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PositionSlotRepository extends JpaRepository<PositionSlot, Long> {

}