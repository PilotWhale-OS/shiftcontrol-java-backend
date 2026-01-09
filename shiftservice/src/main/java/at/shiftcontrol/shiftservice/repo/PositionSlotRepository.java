package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.lib.entity.PositionSlot;

@Repository
public interface PositionSlotRepository extends JpaRepository<PositionSlot, Long> {
}
