package at.shiftcontrol.shiftservice.repo;

import java.util.Optional;

import at.shiftcontrol.shiftservice.entity.ShiftPlanInvite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftPlanInviteRepository extends JpaRepository<ShiftPlanInvite, Long> {
    Optional<ShiftPlanInvite> findByCode(String code);
}
