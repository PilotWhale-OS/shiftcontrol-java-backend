package at.shiftcontrol.shiftservice.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import at.shiftcontrol.lib.entity.ShiftPlanInvite;

public interface ShiftPlanInviteRepository extends JpaRepository<ShiftPlanInvite, Long> {
    Optional<ShiftPlanInvite> findByCode(String code);

    boolean existsByCode(String code);
}
