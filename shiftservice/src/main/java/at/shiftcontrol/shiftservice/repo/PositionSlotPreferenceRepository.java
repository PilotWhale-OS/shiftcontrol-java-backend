package at.shiftcontrol.shiftservice.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import at.shiftcontrol.lib.entity.PositionSlotPreference;
import at.shiftcontrol.lib.entity.PositionSlotPreferenceId;

@Repository
public interface PositionSlotPreferenceRepository extends JpaRepository<PositionSlotPreference, PositionSlotPreferenceId> {
}
